package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_review")
@Schema(description = "商品评价")
public class PmsReview extends BaseEntity {

    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("score")
    @Schema(description = "评分 1-5分")
    private Integer score;

    @TableField("content")
    @Schema(description = "评价内容")
    private String content;

    @TableField("images")
    @Schema(description = "评价图片")
    private String images;

    @TableField("is_anonymous")
    @Schema(description = "是否匿名")
    private Integer isAnonymous;

    @TableField("reply_content")
    @Schema(description = "商家回复")
    private String replyContent;

    @TableField("reply_time")
    @Schema(description = "回复时间")
    private String replyTime;
}
