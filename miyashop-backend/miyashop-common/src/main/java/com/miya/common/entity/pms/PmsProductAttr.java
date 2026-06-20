package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品规格参数表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product_attr")
@Schema(description = "商品规格参数")
public class PmsProductAttr extends BaseEntity {

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("name")
    @Schema(description = "规格名称")
    private String name;

    @TableField("`values`")
    @Schema(description = "规格值(多个逗号分隔)")
    private String values;
}
