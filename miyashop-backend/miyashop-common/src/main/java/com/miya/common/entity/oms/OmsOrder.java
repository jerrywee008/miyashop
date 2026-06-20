package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_order")
@Schema(description = "订单信息")
public class OmsOrder extends BaseEntity {

    @TableField("order_no")
    @Schema(description = "订单号")
    private String orderNo;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("total_amount")
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @TableField("pay_amount")
    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @TableField("discount_amount")
    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @TableField("freight")
    @Schema(description = "运费")
    private BigDecimal freight;

    @TableField("status")
    @Schema(description = "订单状态")
    private Integer status;

    @TableField("pay_type")
    @Schema(description = "支付方式")
    private Integer payType;

    @TableField("pay_time")
    @Schema(description = "支付时间")
    private String payTime;

    @TableField("receiver_name")
    @Schema(description = "收货人姓名")
    private String receiverName;

    @TableField("receiver_phone")
    @Schema(description = "收货人电话")
    private String receiverPhone;

    @TableField("receiver_address")
    @Schema(description = "收货地址")
    private String receiverAddress;

    @TableField("remark")
    @Schema(description = "订单备注")
    private String remark;

    @TableField("cancel_reason")
    @Schema(description = "取消原因")
    private String cancelReason;

    @TableField("cancel_time")
    @Schema(description = "取消时间")
    private String cancelTime;

    @TableField("finish_time")
    @Schema(description = "完成时间")
    private String finishTime;
}