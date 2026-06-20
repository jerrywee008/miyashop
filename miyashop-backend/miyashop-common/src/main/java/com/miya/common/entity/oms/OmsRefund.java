package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 退款记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_refund")
@Schema(description = "退款记录")
public class OmsRefund extends BaseEntity {

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("order_item_id")
    @Schema(description = "订单明细ID")
    private Long orderItemId;

    @TableField("refund_no")
    @Schema(description = "退款单号")
    private String refundNo;

    @TableField("amount")
    @Schema(description = "退款金额")
    private BigDecimal amount;

    @TableField("reason")
    @Schema(description = "退款原因")
    private String reason;

    @TableField("type")
    @Schema(description = "退款类型 1仅退款 2退货退款")
    private Integer type;

    @TableField("status")
    @Schema(description = "退款状态")
    private Integer status;

    @TableField("apply_time")
    @Schema(description = "申请时间")
    private String applyTime;

    @TableField("handle_time")
    @Schema(description = "处理时间")
    private String handleTime;

    @TableField("refund_time")
    @Schema(description = "退款时间")
    private String refundTime;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
