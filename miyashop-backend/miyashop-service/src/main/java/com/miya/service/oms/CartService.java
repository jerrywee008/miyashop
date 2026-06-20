package com.miya.service.oms;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.oms.OmsCart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<OmsCart> {

    /**
     * 获取用户购物车列表
     */
    List<OmsCart> getUserCart(Long userId);

    /**
     * 添加到购物车
     */
    void addToCart(Long userId, Long productId, Long skuId, Integer quantity);

    /**
     * 更新数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 更新选中状态
     */
    void updateSelected(Long userId, Long cartId, Integer selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Long userId, Integer selected);

    /**
     * 删除购物车商品
     */
    void removeItem(Long userId, Long cartId);

    /**
     * 获取购物车商品数量
     */
    Integer getCartCount(Long userId);
}
