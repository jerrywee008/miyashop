package com.miya.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    FAILED(400, "请求参数错误"),

    // 用户相关
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ACCOUNT_DISABLED(1003, "账号已被禁用"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "Token无效"),

    // 商品相关
    PRODUCT_NOT_EXIST(2001, "商品不存在"),
    PRODUCT_OUT_OF_STOCK(2002, "商品库存不足"),
    PRODUCT_OFF_SHELF(2003, "商品已下架"),

    // 订单相关
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_EXPIRED(3003, "订单已过期"),

    // 秒杀相关
    SEILLK_NOT_STARTED(4001, "秒杀活动未开始"),
    SEILLK_ENDED(4002, "秒杀活动已结束"),
    SEILLK_OUT_OF_STOCK(4003, "秒杀商品已售罄"),
    SEILLK_LIMIT_EXCEEDED(4004, "超过秒杀限购数量"),

    // 团购相关
    GROUPBUY_NOT_STARTED(5001, "团购活动未开始"),
    GROUPBUY_ENDED(5002, "团购活动已结束"),
    GROUPBUY_TEAM_FULL(5003, "团购队伍已满"),
    GROUPBUY_TEAM_EXPIRED(5004, "团购队伍已过期"),
    GROUPBUY_TEAM_FAILED(5005, "团购失败"),

    // 支付相关
    PAYMENT_FAILED(6001, "支付失败"),
    PAYMENT_EXPIRED(6002, "支付已过期"),
    PAYMENT_NOT_FOUND(6003, "支付记录不存在");

    private final Integer code;
    private final String message;
}