package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_order_item")
@Schema(description = "订单明细")
public class OmsOrderItem extends BaseEntity {

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("product_name")
    @Schema(description = "商品名称")
    private String productName;

    @TableField("sku_image")
    @Schema(description = "SKU图片")
    private String skuImage;

    @TableField("sku_specs")
    @Schema(description = "SKU规格")
    private String skuSpecs;

    @TableField("price")
    @Schema(description = "商品单价")
    private BigDecimal price;

    @TableField("quantity")
    @Schema(description = "购买数量")
    private Integer quantity;

    @TableField("total_price")
    @Schema(description = "小计金额")
    private BigDecimal totalPrice;
}
