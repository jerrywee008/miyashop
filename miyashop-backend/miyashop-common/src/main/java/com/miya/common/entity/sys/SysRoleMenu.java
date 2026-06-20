package com.miya.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
@Schema(description = "角色菜单关联")
public class SysRoleMenu extends BaseEntity {

    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    @TableField("menu_id")
    @Schema(description = "菜单ID")
    private Long menuId;
}
