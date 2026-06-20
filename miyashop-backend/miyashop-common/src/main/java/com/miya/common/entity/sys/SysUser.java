package com.miya.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "管理员用户")
public class SysUser extends BaseEntity {

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;

    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    @TableField("avatar")
    @Schema(description = "头像")
    private String avatar;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    @TableField("status")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private String lastLoginTime;

    @TableField("last_login_ip")
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}
