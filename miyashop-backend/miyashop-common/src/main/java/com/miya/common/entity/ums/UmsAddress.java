package com.miya.common.entity.ums;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户收货地址表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_address")
@Schema(description = "收货地址")
public class UmsAddress extends BaseEntity {

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("name")
    @Schema(description = "收货人姓名")
    private String name;

    @TableField("phone")
    @Schema(description = "收货人电话")
    private String phone;

    @TableField("province")
    @Schema(description = "省份")
    private String province;

    @TableField("city")
    @Schema(description = "城市")
    private String city;

    @TableField("district")
    @Schema(description = "区/县")
    private String district;

    @TableField("detail")
    @Schema(description = "详细地址")
    private String detail;

    @TableField("is_default")
    @Schema(description = "是否默认 0否 1是")
    private Integer isDefault;
}
