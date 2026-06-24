package com.miya.service.coupon;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.pms.PmsCoupon;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<PmsCoupon> {

    /**
     * 分页查询优惠券列表
     */
    Page<PmsCoupon> pageList(Integer page, Integer size, String name, Integer type, Integer status);
}
