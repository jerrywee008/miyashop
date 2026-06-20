package com.miya.service.sms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.sms.SmsSeckillActivity;

import java.util.Map;

/**
 * 秒杀服务接口
 */
public interface SeckillService extends IService<SmsSeckillActivity> {

    /**
     * 获取秒杀活动列表
     */
    Page<SmsSeckillActivity> pageActivities(Integer page, Integer size, Integer status);

    /**
     * 秒杀下单
     */
    Map<String, Object> seckillOrder(Long userId, Long activityId, Long skuId, Integer quantity);
}
