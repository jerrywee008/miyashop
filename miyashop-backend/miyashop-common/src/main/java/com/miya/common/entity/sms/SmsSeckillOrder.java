package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 秒杀订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_seckill_order")
@Schema(description = "秒杀订单")
public class SmsSeckillOrder extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("activity_id")
    @Schema(description = "活动ID")
    private Long activityId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("quantity")
    @Schema(description = "购买数量")
    private Integer quantity;

    @TableField("seckill_price")
    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @TableField("total_amount")
    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @TableField("status")
    @Schema(description = "状态 0待支付 1已支付 2已取消")
    private Integer status;
}
