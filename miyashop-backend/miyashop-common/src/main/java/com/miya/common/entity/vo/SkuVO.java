package com.miya.common.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU视图对象（含商品名称）
 */
@Data
@Schema(description = "SKU视图对象")
public class SkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "显示名称（商品名 + 规格）")
    private String name;

    @Schema(description = "SKU图片")
    private String image;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "规格参数")
    private String specs;

    @Schema(description = "状态 0下架 1上架")
    private Integer status;

    @Schema(description = "SPU ID")
    private Long spuId;
}
