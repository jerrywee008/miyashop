package com.miya.common.constant;

/**
 * Redis常量
 */
public class RedisConstants {

    /**
     * Token缓存前缀
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * 用户信息缓存前缀
     */
    public static final String USER_PREFIX = "user:";

    /**
     * 商品缓存前缀
     */
    public static final String PRODUCT_PREFIX = "product:";

    /**
     * 分类缓存前缀
     */
    public static final String CATEGORY_PREFIX = "category:";

    /**
     * 购物车缓存前缀
     */
    public static final String CART_PREFIX = "cart:";

    /**
     * 秒杀库存缓存前缀
     */
    public static final String SEILLK_STOCK_PREFIX = "seckill:stock:";

    /**
     * 秒杀用户购买记录缓存前缀
     */
    public static final String SEILLK_USER_PREFIX = "seckill:user:";

    /**
     * 团购队伍缓存前缀
     */
    public static final String GROUPBUY_TEAM_PREFIX = "groupbuy:team:";

    /**
     * 团购成员缓存前缀
     */
    public static final String GROUPBUY_MEMBER_PREFIX = "groupbuy:member:";

    /**
     * 分布式锁前缀
     */
    public static final String LOCK_PREFIX = "lock:";

    /**
     * Token过期时间 (秒)
     */
    public static final long TOKEN_EXPIRE = 86400;

    /**
     * 用户信息过期时间 (秒)
     */
    public static final long USER_EXPIRE = 3600;

    /**
     * 商品缓存过期时间 (秒)
     */
    public static final long PRODUCT_EXPIRE = 7200;

    /**
     * 分类缓存过期时间 (秒)
     */
    public static final long CATEGORY_EXPIRE = 86400;

    /**
     * 购物车过期时间 (秒)
     */
    public static final long CART_EXPIRE = 604800;

    /**
     * 秒杀用户购买记录过期时间 (秒) - 30天
     */
    public static final long SEILLK_USER_EXPIRE = 2592000;
}