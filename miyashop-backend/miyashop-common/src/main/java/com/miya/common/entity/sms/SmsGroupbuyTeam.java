package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团购队伍表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_groupbuy_team")
@Schema(description = "团购队伍")
public class SmsGroupbuyTeam extends BaseEntity {

    @TableField("activity_id")
    @Schema(description = "活动ID")
    private Long activityId;

    @TableField("sku_id")
    @Schema(description = "SKU ID")
    private Long skuId;

    @TableField("leader_id")
    @Schema(description = "团长ID")
    private Long leaderId;

    @TableField("current_people")
    @Schema(description = "当前人数")
    private Integer currentPeople;

    @TableField("max_people")
    @Schema(description = "最大人数")
    private Integer maxPeople;

    @TableField("status")
    @Schema(description = "状态")
    private Integer status;

    @TableField("expire_time")
    @Schema(description = "过期时间")
    private String expireTime;

    @TableField("success_time")
    @Schema(description = "成团时间")
    private String successTime;
}