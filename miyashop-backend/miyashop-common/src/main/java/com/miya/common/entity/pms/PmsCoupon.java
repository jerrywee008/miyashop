package com.miya.common.entity.pms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_coupon")
@Schema(description = "优惠券")
public class PmsCoupon extends BaseEntity {

    @TableField("name")
    @Schema(description = "优惠券名称")
    private String name;

    @TableField("type")
    @Schema(description = "类型 1满减券 2折扣券")
    private Integer type;

    @TableField("amount")
    @Schema(description = "面额")
    private BigDecimal amount;

    @TableField("discount")
    @Schema(description = "折扣(如85表示85折)")
    private Integer discount;

    @TableField("min_amount")
    @Schema(description = "最低消费金额")
    private BigDecimal minAmount;

    @TableField("total_count")
    @Schema(description = "发放总数")
    private Integer totalCount;

    @TableField("used_count")
    @Schema(description = "已使用数量")
    private Integer usedCount;

    @TableField("per_limit")
    @Schema(description = "每人限领数量")
    private Integer perLimit;

    @TableField("start_time")
    @Schema(description = "开始时间")
    private String startTime;

    @TableField("end_time")
    @Schema(description = "结束时间")
    private String endTime;

    @TableField("status")
    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
