package com.miya.common.entity.ums;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member")
@Schema(description = "会员信息")
public class UmsMember extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @TableField("mobile")
    @Schema(description = "手机号")
    private String mobile;

    @TableField("nickname")
    @Schema(description = "昵称")
    private String nickname;

    @TableField("avatar")
    @Schema(description = "头像")
    private String avatar;

    @TableField("gender")
    @Schema(description = "性别 0未知 1男 2女")
    private Integer gender;

    @TableField("birthday")
    @Schema(description = "生日")
    private String birthday;

    @TableField("openid")
    @Schema(description = "微信OpenID")
    private String openid;

    @TableField("level")
    @Schema(description = "会员等级")
    private Integer level;

    @TableField("points")
    @Schema(description = "积分")
    private Integer points;

    @TableField("balance")
    @Schema(description = "余额")
    private BigDecimal balance;

    @TableField("total_spent")
    @Schema(description = "累计消费")
    private BigDecimal totalSpent;

    @TableField("status")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private String lastLoginTime;
}