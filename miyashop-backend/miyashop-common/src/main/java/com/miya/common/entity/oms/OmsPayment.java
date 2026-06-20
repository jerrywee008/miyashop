package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 支付记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_payment")
@Schema(description = "支付记录")
public class OmsPayment extends BaseEntity {

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("payment_no")
    @Schema(description = "支付单号")
    private String paymentNo;

    @TableField("third_party_no")
    @Schema(description = "第三方支付单号")
    private String thirdPartyNo;

    @TableField("amount")
    @Schema(description = "支付金额")
    private BigDecimal amount;

    @TableField("pay_type")
    @Schema(description = "支付方式 1微信 2支付宝")
    private Integer payType;

    @TableField("status")
    @Schema(description = "支付状态 0待支付 1已支付 2支付失败 3已退款")
    private Integer status;

    @TableField("pay_time")
    @Schema(description = "支付时间")
    private String payTime;

    @TableField("refund_amount")
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @TableField("refund_time")
    @Schema(description = "退款时间")
    private String refundTime;
}
