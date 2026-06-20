package com.miya.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色")
public class SysRole extends BaseEntity {

    @TableField("name")
    @Schema(description = "角色名称")
    private String name;

    @TableField("code")
    @Schema(description = "角色编码")
    private String code;

    @TableField("description")
    @Schema(description = "角色描述")
    private String description;

    @TableField("status")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;
}
