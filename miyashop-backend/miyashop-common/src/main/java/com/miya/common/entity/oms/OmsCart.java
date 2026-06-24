package com.miya.common.entity.oms;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购物车表（oms_cart 表无 deleted/created_time/updated_time 字段，不继承 BaseEntity）
 */
@Data
@TableName("oms_cart")
@Schema(description = "购物车")
public class OmsCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

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
