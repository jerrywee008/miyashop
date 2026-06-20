package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团购成员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_groupbuy_member")
@Schema(description = "团购成员")
public class SmsGroupbuyMember extends BaseEntity {

    @TableField("team_id")
    @Schema(description = "队伍ID")
    private Long teamId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("join_time")
    @Schema(description = "加入时间")
    private String joinTime;

    @TableField("is_leader")
    @Schema(description = "是否团长 0否 1是")
    private Integer isLeader;
}
