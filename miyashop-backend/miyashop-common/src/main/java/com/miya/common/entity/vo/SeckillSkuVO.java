package com.miya.common.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀SKU视图对象（含SKU基本信息）
 */
@Data
@Schema(description = "秒杀SKU视图对象")
public class SeckillSkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "SKU图片")
    private String image;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀库存")
    private Integer stock;

    @Schema(description = "已售数量")
    private Integer sold;

    @Schema(description = "排序")
    private Integer sort;
}
