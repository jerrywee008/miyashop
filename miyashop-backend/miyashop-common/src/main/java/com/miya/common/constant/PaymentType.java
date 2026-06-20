package com.miya.common.constant;

/**
 * 支付类型常量
 */
public enum PaymentType {

    /**
     * 微信支付
     */
    WECHAT(1, "微信支付"),

    /**
     * 支付宝
     */
    ALIPAY(2, "支付宝");

    private final Integer code;
    private final String desc;

    PaymentType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentType getByCode(Integer code) {
        for (PaymentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}