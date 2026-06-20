package com.miya.common.constant;

/**
 * 团购状态常量
 */
public enum GroupBuyStatus {

    /**
     * 待开团
     */
    PENDING(0, "待开团"),

    /**
     * 进行中
     */
    ONGOING(1, "进行中"),

    /**
     * 成功
     */
    SUCCESS(2, "成功"),

    /**
     * 失败
     */
    FAILED(3, "失败");

    private final Integer code;
    private final String desc;

    GroupBuyStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static GroupBuyStatus getByCode(Integer code) {
        for (GroupBuyStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}