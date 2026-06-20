package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_sku")
@Schema(description = "商品SKU")
public class PmsSku extends BaseEntity {

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("spu_id")
    @Schema(description = "SPU ID")
    private Long spuId;

    @TableField("specs")
    @Schema(description = "规格参数")
    private String specs;

    @TableField("image")
    @Schema(description = "SKU图片")
    private String image;

    @TableField("price")
    @Schema(description = "价格")
    private BigDecimal price;

    @TableField("original_price")
    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @TableField("stock")
    @Schema(description = "库存")
    private Integer stock;

    @TableField("sales")
    @Schema(description = "销量")
    private Integer sales;

    @TableField("barcode")
    @Schema(description = "条形码")
    private String barcode;

    @TableField("status")
    @Schema(description = "状态 0下架 1上架")
    private Integer status;
}