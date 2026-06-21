package com.miya.common.entity.vo;

import com.miya.common.entity.sms.SmsGroupbuyActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团购活动视图对象（含商品信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "团购活动视图对象")
public class GroupBuyActivityVO extends SmsGroupbuyActivity {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;
}
