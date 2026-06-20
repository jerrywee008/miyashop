package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 品牌表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_brand")
@Schema(description = "品牌信息")
public class PmsBrand extends BaseEntity {

    @TableField("name")
    @Schema(description = "品牌名称")
    private String name;

    @TableField("logo")
    @Schema(description = "品牌logo")
    private String logo;

    @TableField("`desc`")
    @Schema(description = "品牌描述")
    private String desc;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("show_status")
    @Schema(description = "显示状态 0隐藏 1显示")
    private Integer showStatus;
}
