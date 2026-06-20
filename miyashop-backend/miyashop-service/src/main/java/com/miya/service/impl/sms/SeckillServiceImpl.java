package com.miya.service.impl.sms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.pms.PmsSku;
import com.miya.common.entity.sms.SmsSeckillActivity;
import com.miya.common.entity.sms.SmsSeckillOrder;
import com.miya.common.entity.sms.SmsSeckillSku;
import com.miya.common.constant.ResultCode;
import com.miya.common.exception.BusinessException;
import com.miya.mapper.pms.PmsSkuMapper;
import com.miya.mapper.sms.SmsSeckillActivityMapper;
import com.miya.mapper.sms.SmsSeckillOrderMapper;
import com.miya.mapper.sms.SmsSeckillSkuMapper;
import com.miya.service.sms.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 秒杀服务实现
 */
@Slf4j
@Service
public class SeckillServiceImpl extends ServiceImpl<SmsSeckillActivityMapper, SmsSeckillActivity> implements SeckillService {

    @Autowired
    private SmsSeckillSkuMapper seckillSkuMapper;
    @Autowired
    private SmsSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private PmsSkuMapper skuMapper;
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // Redis秒杀库存Key前缀
    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    // 秒杀下单用户Key前缀
    private static final String SECKILL_USER_PREFIX = "seckill:user:";

    // Lua脚本：原子性扣减秒杀库存
    private static final String DEDUCT_STOCK_LUA =
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock and tonumber(stock) > 0 then " +
            "    redis.call('decr', KEYS[1]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    @Override
    public Page<SmsSeckillActivity> pageActivities(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<SmsSeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SmsSeckillActivity::getStatus, status);
        }
        wrapper.orderByDesc(SmsSeckillActivity::getSort);
        wrapper.orderByDesc(SmsSeckillActivity::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> seckillOrder(Long userId, Long activityId, Long skuId, Integer quantity) {
        // 1. 检查活动
        SmsSeckillActivity activity = getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException(ResultCode.SEILLK_NOT_STARTED);
        }

        // 2. 检查活动时间
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (now.compareTo(activity.getStartTime()) < 0) {
            throw new BusinessException(ResultCode.SEILLK_NOT_STARTED);
        }
        if (now.compareTo(activity.getEndTime()) > 0) {
            throw new BusinessException(ResultCode.SEILLK_ENDED);
        }

        // 3. 查询秒杀SKU
        LambdaQueryWrapper<SmsSeckillSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(SmsSeckillSku::getActivityId, activityId);
        skuWrapper.eq(SmsSeckillSku::getSkuId, skuId);
        SmsSeckillSku seckillSku = seckillSkuMapper.selectOne(skuWrapper);
        if (seckillSku == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 4. 检查库存 (lua脚本原子操作)
        if (redisTemplate != null) {
            String stockKey = SECKILL_STOCK_PREFIX + activityId + ":" + skuId;
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(stockKey));
            if (result == null || result == 0) {
                throw new BusinessException(ResultCode.SEILLK_OUT_OF_STOCK);
            }
        } else {
            // 无Redis时直接检查数据库
            if (seckillSku.getStock() - seckillSku.getSold() < quantity) {
                throw new BusinessException(ResultCode.SEILLK_OUT_OF_STOCK);
            }
        }

        // 5. 限购检查
        if (seckillSku.getLimitPerUser() != null && seckillSku.getLimitPerUser() > 0) {
            LambdaQueryWrapper<SmsSeckillOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(SmsSeckillOrder::getUserId, userId);
            orderWrapper.eq(SmsSeckillOrder::getActivityId, activityId);
            orderWrapper.eq(SmsSeckillOrder::getSkuId, skuId);
            long boughtCount = seckillOrderMapper.selectCount(orderWrapper);
            if (boughtCount >= seckillSku.getLimitPerUser()) {
                throw new BusinessException(ResultCode.SEILLK_LIMIT_EXCEEDED);
            }
        }

        // 6. 扣减库存
        seckillSku.setSold(seckillSku.getSold() + quantity);
        seckillSkuMapper.updateById(seckillSku);

        // 7. 创建秒杀订单
        BigDecimal totalAmount = seckillSku.getSeckillPrice().multiply(BigDecimal.valueOf(quantity));
        SmsSeckillOrder seckillOrder = new SmsSeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setActivityId(activityId);
        seckillOrder.setSkuId(skuId);
        seckillOrder.setQuantity(quantity);
        seckillOrder.setSeckillPrice(seckillSku.getSeckillPrice());
        seckillOrder.setTotalAmount(totalAmount);
        seckillOrder.setStatus(0); // 待支付
        seckillOrderMapper.insert(seckillOrder);

        log.info("秒杀下单成功: userId={}, activityId={}, skuId={}", userId, activityId, skuId);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", seckillOrder.getId());
        result.put("orderNo", "SK" + System.currentTimeMillis());
        result.put("payAmount", totalAmount);
        result.put("expireTime", System.currentTimeMillis() + 15 * 60 * 1000);
        return result;
    }
}
