package com.miya.common.entity.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miya.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_seckill_activity")
@Schema(description = "秒杀活动")
public class SmsSeckillActivity extends BaseEntity {

    @TableField("name")
    @Schema(description = "活动名称")
    private String name;

    @TableField("start_time")
    @Schema(description = "开始时间")
    private String startTime;

    @TableField("end_time")
    @Schema(description = "结束时间")
    private String endTime;

    @TableField("status")
    @Schema(description = "活动状态")
    private Integer status;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;
}