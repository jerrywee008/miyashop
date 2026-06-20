package com.miya.common.constant;

/**
 * 秒杀状态常量
 */
public enum SeckillStatus {

    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),

    /**
     * 进行中
     */
    ONGOING(1, "进行中"),

    /**
     * 已结束
     */
    ENDED(2, "已结束");

    private final Integer code;
    private final String desc;

    SeckillStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SeckillStatus getByCode(Integer code) {
        for (SeckillStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}