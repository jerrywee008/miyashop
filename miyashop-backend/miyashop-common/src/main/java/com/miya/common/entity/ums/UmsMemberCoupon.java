package com.miya.common.entity.ums;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member_coupon")
@Schema(description = "会员优惠券")
public class UmsMemberCoupon extends BaseEntity {

    @TableField("member_id")
    @Schema(description = "会员ID")
    private Long memberId;

    @TableField("coupon_id")
    @Schema(description = "优惠券ID")
    private Long couponId;

    @TableField("order_id")
    @Schema(description = "使用的订单ID")
    private Long orderId;

    @TableField("status")
    @Schema(description = "状态 0未使用 1已使用 2已过期")
    private Integer status;

    @TableField("receive_time")
    @Schema(description = "领取时间")
    private String receiveTime;

    @TableField("use_time")
    @Schema(description = "使用时间")
    private String useTime;

    @TableField("expire_time")
    @Schema(description = "过期时间")
    private String expireTime;
}
