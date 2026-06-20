package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_cart")
@Schema(description = "购物车")
public class OmsCart extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("quantity")
    @Schema(description = "数量")
    private Integer quantity;

    @TableField("selected")
    @Schema(description = "是否选中 0未选中 1已选中")
    private Integer selected;
}
