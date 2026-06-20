package com.miya.web.controller.oms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.oms.OmsOrder;
import com.miya.common.entity.oms.OmsOrderItem;
import com.miya.common.result.Result;
import com.miya.service.oms.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        Long userId = 1L; // TODO: 从Token获取
        Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
        Integer payType = params.get("payType") != null ? Integer.valueOf(params.get("payType").toString()) : 1;
        Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        // 传递完整地址信息作为 fallback
        Map<String, Object> address = params.get("address") != null
            ? (Map<String, Object>) params.get("address") : null;

        Map<String, Object> result = orderService.createOrder(userId, addressId, items, payType, couponId, remark, address);
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取订单列表")
    public Result<Page<OmsOrder>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        return Result.success(orderService.pageList(page, size, userId, orderNo, status, startTime, endTime));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<OmsOrder> detail(@PathVariable Long id) {
        OmsOrder order = orderService.getDetail(id);
        return Result.success(order);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "获取订单商品明细")
    public Result<List<OmsOrderItem>> items(@PathVariable Long id) {
        return Result.success(orderService.getOrderItems(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancel(@PathVariable Long id,
                                @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        Long userId = 1L;
        orderService.cancelOrder(userId, id, reason);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "支付订单")
    public Result<Void> pay(@PathVariable Long id,
                             @RequestBody Map<String, Object> params) {
        Integer payType = Integer.valueOf(params.get("payType").toString());
        String paymentNo = params.get("paymentNo").toString();
        orderService.payOrder(id, payType, paymentNo);
        return Result.success();
    }

    @PutMapping("/{id}/ship")
    @Operation(summary = "发货")
    public Result<Void> ship(@PathVariable Long id,
                              @RequestBody Map<String, Object> params) {
        String trackingNo = params.get("trackingNo").toString();
        String logisticsCompany = params.get("logisticsCompany").toString();
        orderService.shipOrder(id, trackingNo, logisticsCompany);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货")
    public Result<Void> confirm(@PathVariable Long id) {
        Long userId = 1L;
        orderService.confirmOrder(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/refund")
    @Operation(summary = "申请退款")
    public Result<Void> refund(@PathVariable Long id,
                                @RequestBody Map<String, Object> params) {
        Long userId = 1L;
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1;
        java.math.BigDecimal amount = params.get("amount") != null
                ? new java.math.BigDecimal(params.get("amount").toString())
                : java.math.BigDecimal.ZERO;
        orderService.applyRefund(userId, id, reason, type, amount);
        return Result.success();
    }
}
