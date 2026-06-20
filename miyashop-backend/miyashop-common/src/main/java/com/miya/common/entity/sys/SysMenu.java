package com.miya.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单权限")
public class SysMenu extends BaseEntity {

    @TableField("parent_id")
    @Schema(description = "父菜单ID")
    private Long parentId;

    @TableField("name")
    @Schema(description = "菜单名称")
    private String name;

    @TableField("url")
    @Schema(description = "菜单URL")
    private String url;

    @TableField("icon")
    @Schema(description = "菜单图标")
    private String icon;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("type")
    @Schema(description = "类型 1目录 2菜单 3按钮")
    private Integer type;

    @TableField("permission")
    @Schema(description = "权限标识")
    private String permission;

    @TableField("status")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;
}
