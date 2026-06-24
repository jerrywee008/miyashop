package com.miya.service.impl.coupon;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.pms.PmsCoupon;
import com.miya.mapper.pms.PmsCouponMapper;
import com.miya.service.coupon.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 优惠券服务实现
 */
@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<PmsCouponMapper, PmsCoupon> implements CouponService {

    @Override
    public Page<PmsCoupon> pageList(Integer page, Integer size, String name, Integer type, Integer status) {
        LambdaQueryWrapper<PmsCoupon> wrapper = new LambdaQueryWrapper<>();
        // 兼容 deleted 为 null 的记录（数据库默认值问题）
        wrapper.and(w -> w.eq(PmsCoupon::getDeleted, 0).or().isNull(PmsCoupon::getDeleted));
        if (StringUtils.hasText(name)) {
            wrapper.like(PmsCoupon::getName, name);
        }
        if (type != null) {
            wrapper.eq(PmsCoupon::getType, type);
        }
        if (status != null) {
            wrapper.eq(PmsCoupon::getStatus, status);
        }
        wrapper.orderByDesc(PmsCoupon::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }
}
