package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 秒杀商品关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_seckill_sku")
@Schema(description = "秒杀商品关联")
public class SmsSeckillSku extends BaseEntity {

    @TableField("activity_id")
    @Schema(description = "活动ID")
    private Long activityId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("seckill_price")
    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @TableField("stock")
    @Schema(description = "秒杀库存")
    private Integer stock;

    @TableField("sold")
    @Schema(description = "已售数量")
    private Integer sold;

    @TableField("limit_per_user")
    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;
}
