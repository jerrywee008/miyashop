package com.miya.service.oms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.oms.OmsOrder;
import com.miya.common.entity.oms.OmsOrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<OmsOrder> {

    /**
     * 创建订单
     */
    Map<String, Object> createOrder(Long userId, Long addressId, List<Map<String, Object>> items,
                                     Integer payType, Long couponId, String remark,
                                     Map<String, Object> addressFallback);

    /**
     * 分页查询订单列表
     */
    Page<OmsOrder> pageList(Integer page, Integer size, Long userId, String orderNo,
                            Integer status, String startTime, String endTime);

    /**
     * 获取订单详情
     */
    OmsOrder getDetail(Long orderId);

    /**
     * 获取订单商品明细
     */
    List<OmsOrderItem> getOrderItems(Long orderId);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId, String reason);

    /**
     * 支付订单
     */
    void payOrder(Long orderId, Integer payType, String paymentNo);

    /**
     * 发货
     */
    void shipOrder(Long orderId, String trackingNo, String logisticsCompany);

    /**
     * 确认收货
     */
    void confirmOrder(Long userId, Long orderId);

    /**
     * 申请退款
     */
    void applyRefund(Long userId, Long orderId, String reason, Integer type, BigDecimal amount);
}
