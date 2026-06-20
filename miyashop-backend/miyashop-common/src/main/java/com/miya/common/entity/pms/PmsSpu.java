package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品SPU表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_spu")
@Schema(description = "商品SPU")
public class PmsSpu extends BaseEntity {

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("spec_params")
    @Schema(description = "规格参数定义(JSON格式)")
    private String specParams;
}
