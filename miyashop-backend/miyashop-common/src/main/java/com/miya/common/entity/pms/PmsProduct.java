package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product")
@Schema(description = "商品信息")
public class PmsProduct extends BaseEntity {

    @TableField("name")
    @Schema(description = "商品名称")
    private String name;

    @TableField("category_id")
    @Schema(description = "分类ID")
    private Long categoryId;

    @TableField("brand_id")
    @Schema(description = "品牌ID")
    private Long brandId;

    @TableField("images")
    @Schema(description = "商品图片")
    private String images;

    @TableField("detail")
    @Schema(description = "商品详情")
    private String detail;

    @TableField("price")
    @Schema(description = "参考价格")
    private BigDecimal price;

    @TableField("stock")
    @Schema(description = "总库存")
    private Integer stock;

    @TableField("sales")
    @Schema(description = "销量")
    private Integer sales;

    @TableField("score")
    @Schema(description = "评分")
    private BigDecimal score;

    @TableField("review_count")
    @Schema(description = "评价数")
    private Integer reviewCount;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("status")
    @Schema(description = "状态 0下架 1上架")
    private Integer status;
}