package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_category")
@Schema(description = "商品分类")
public class PmsCategory extends BaseEntity {

    @TableField("parent_id")
    @Schema(description = "父分类ID")
    private Long parentId;

    @TableField("name")
    @Schema(description = "分类名称")
    private String name;

    @TableField("icon")
    @Schema(description = "分类图标")
    private String icon;

    @TableField("image")
    @Schema(description = "分类图片")
    private String image;

    @TableField("level")
    @Schema(description = "分类层级 1一级 2二级 3三级")
    private Integer level;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("show_status")
    @Schema(description = "显示状态 0隐藏 1显示")
    private Integer showStatus;
}
