package com.miya.service.impl.oms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.oms.*;
import com.miya.common.entity.pms.PmsProduct;
import com.miya.common.entity.pms.PmsSku;
import com.miya.common.entity.ums.UmsAddress;
import com.miya.common.constant.ResultCode;
import com.miya.common.exception.BusinessException;
import com.miya.mapper.oms.*;
import com.miya.mapper.pms.PmsProductMapper;
import com.miya.mapper.pms.PmsSkuMapper;
import com.miya.mapper.ums.UmsAddressMapper;
import com.miya.service.oms.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单服务实现
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OrderService {

    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderItemMapper orderItemMapper;
    @Autowired
    private OmsCartMapper cartMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsSkuMapper skuMapper;
    @Autowired
    private UmsAddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, Long addressId, List<Map<String, Object>> items,
                                            Integer payType, Long couponId, String remark,
                                            Map<String, Object> addressFallback) {
        // 获取地址信息：优先查数据库，fallback 使用前端传来的地址
        String receiverName, receiverPhone, receiverAddress;
        if (addressId != null) {
            UmsAddress address = addressMapper.selectById(addressId);
            if (address != null) {
                receiverName = address.getName();
                receiverPhone = address.getPhone();
                receiverAddress = address.getProvince() + address.getCity() + address.getDistrict() + " " + address.getDetail();
            } else if (addressFallback != null) {
                receiverName = (String) addressFallback.getOrDefault("name", "用户");
                receiverPhone = (String) addressFallback.getOrDefault("phone", "");
                receiverAddress = (String) addressFallback.getOrDefault("fullAddress", "");
            } else {
                throw new BusinessException("收货地址不存在");
            }
        } else if (addressFallback != null) {
            receiverName = (String) addressFallback.getOrDefault("name", "用户");
            receiverPhone = (String) addressFallback.getOrDefault("phone", "");
            receiverAddress = (String) addressFallback.getOrDefault("fullAddress", "");
        } else {
            throw new BusinessException("收货地址不存在");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsOrderItem> orderItems = new ArrayList<>();

        // 构造订单明细
        for (Map<String, Object> item : items) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Long productId = item.get("productId") != null ? Long.valueOf(item.get("productId").toString()) : null;
            Integer quantity = Integer.valueOf(item.get("quantity").toString());

            String productName;
            String skuImage;
            String skuSpecs;
            BigDecimal price;

            PmsSku sku = skuMapper.selectById(skuId);
            if (sku != null) {
                // 数据库存在：使用数据库数据 + 扣库存
                if (sku.getStock() < quantity) {
                    throw new BusinessException(ResultCode.PRODUCT_OUT_OF_STOCK);
                }
                PmsProduct product = productMapper.selectById(sku.getProductId());
                productName = product != null ? product.getName() : "商品";
                skuImage = sku.getImage();
                skuSpecs = sku.getSpecs();
                price = sku.getPrice();

                // 扣减库存
                sku.setStock(sku.getStock() - quantity);
                sku.setSales(sku.getSales() + quantity);
                skuMapper.updateById(sku);
            } else {
                // 数据库不存在：使用前端传来的数据（开发/测试环境 fallback）
                productName = item.get("name") != null ? item.get("name").toString() : "商品";
                skuImage = item.get("image") != null ? item.get("image").toString() : null;
                skuSpecs = item.get("specs") != null ? item.get("specs").toString() : null;
                String priceStr = item.get("price") != null ? item.get("price").toString() : "0";
                price = new BigDecimal(priceStr);
            }

            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductId(productId);
            orderItem.setSkuId(skuId);
            orderItem.setProductName(productName);
            orderItem.setSkuImage(skuImage);
            orderItem.setSkuSpecs(skuSpecs);
            orderItem.setPrice(price);
            orderItem.setQuantity(quantity);
            orderItem.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));

            totalAmount = totalAmount.add(orderItem.getTotalPrice());
            orderItems.add(orderItem);
        }

        // 计算优惠
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (totalAmount.compareTo(new BigDecimal("300")) >= 0) {
            discountAmount = new BigDecimal("50"); // 满300减50
        }

        BigDecimal freight = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.subtract(discountAmount).add(freight);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 创建订单
        OmsOrder order = new OmsOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setDiscountAmount(discountAmount);
        order.setFreight(freight);
        order.setStatus(0); // 待支付
        order.setPayType(payType);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);

        save(order);

        // 保存订单明细
        for (OmsOrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 清除购物车中已下单的商品
        for (Map<String, Object> item : items) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OmsCart::getUserId, userId);
            wrapper.eq(OmsCart::getSkuId, skuId);
            cartMapper.delete(wrapper);
        }

        log.info("订单创建成功: orderNo={}, userId={}, payAmount={}", orderNo, userId, payAmount);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", orderNo);
        result.put("payAmount", payAmount);
        result.put("expireTime", System.currentTimeMillis() + 15 * 60 * 1000); // 15分钟支付超时
        return result;
    }

    @Override
    public Page<OmsOrder> pageList(Integer page, Integer size, Long userId, String orderNo,
                                    Integer status, String startTime, String endTime) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OmsOrder::getUserId, userId);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(OmsOrder::getOrderNo, orderNo);
        }
        if (status != null && status >= 0) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OmsOrder::getCreatedTime, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OmsOrder::getCreatedTime, endTime);
        }
        wrapper.orderByDesc(OmsOrder::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public OmsOrder getDetail(Long orderId) {
        return getById(orderId);
    }

    @Override
    public List<OmsOrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        OmsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(4); // 已取消
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateById(order);

        // 恢复库存
        restoreStock(orderId);
        log.info("订单取消: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Integer payType, String paymentNo) {
        OmsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(1); // 待发货
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateById(order);

        // 创建支付记录
        OmsPayment payment = new OmsPayment();
        payment.setOrderId(orderId);
        payment.setPaymentNo(paymentNo);
        payment.setAmount(order.getPayAmount());
        payment.setPayType(payType);
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // paymentMapper.insert(payment);

        log.info("订单支付成功: orderNo={}, paymentNo={}", order.getOrderNo(), paymentNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String trackingNo, String logisticsCompany) {
        OmsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(2); // 待收货
        updateById(order);
        log.info("订单发货: orderNo={}, logisticsCompany={}, trackingNo={}", order.getOrderNo(), logisticsCompany, trackingNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long userId, Long orderId) {
        OmsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateById(order);
        log.info("订单确认收货: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Long orderId, String reason, Integer type, BigDecimal amount) {
        OmsOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() < 1 || order.getStatus() > 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(6); // 售后中
        updateById(order);

        // 创建退款记录
        OmsRefund refund = new OmsRefund();
        refund.setOrderId(orderId);
        refund.setRefundNo("RF" + System.currentTimeMillis());
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setType(type);
        refund.setStatus(0); // 待处理
        // refundMapper.insert(refund);

        log.info("退款申请: orderNo={}, amount={}", order.getOrderNo(), amount);
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    private void restoreStock(Long orderId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getOrderId, orderId);
        List<OmsOrderItem> items = orderItemMapper.selectList(wrapper);
        for (OmsOrderItem item : items) {
            PmsSku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setSales(Math.max(0, sku.getSales() - item.getQuantity()));
                skuMapper.updateById(sku);
            }
        }
    }
}
