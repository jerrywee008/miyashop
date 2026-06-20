package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 团购活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_groupbuy_activity")
@Schema(description = "团购活动")
public class SmsGroupbuyActivity extends BaseEntity {

    @TableField("name")
    @Schema(description = "活动名称")
    private String name;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("original_price")
    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @TableField("groupbuy_price")
    @Schema(description = "团购价")
    private BigDecimal groupbuyPrice;

    @TableField("min_people")
    @Schema(description = "成团人数")
    private Integer minPeople;

    @TableField("max_people")
    @Schema(description = "最大人数")
    private Integer maxPeople;

    @TableField("start_time")
    @Schema(description = "开始时间")
    private String startTime;

    @TableField("end_time")
    @Schema(description = "结束时间")
    private String endTime;

    @TableField("valid_hours")
    @Schema(description = "有效时长(小时)")
    private Integer validHours;

    @TableField("status")
    @Schema(description = "活动状态 0未开始 1进行中 2已结束")
    private Integer status;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;
}
