package com.miya.service.impl.oms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.oms.OmsCart;
import com.miya.mapper.oms.OmsCartMapper;
import com.miya.service.oms.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务实现
 */
@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<OmsCartMapper, OmsCart> implements CartService {

    @Override
    public List<OmsCart> getUserCart(Long userId) {
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCart::getUserId, userId);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, Long productId, Long skuId, Integer quantity) {
        // Check if SKU already exists in cart
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCart::getUserId, userId);
        wrapper.eq(OmsCart::getSkuId, skuId);
        OmsCart existing = getOne(wrapper);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            updateById(existing);
        } else {
            OmsCart cart = new OmsCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setSkuId(skuId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            save(cart);
        }
        log.info("添加购物车: userId={}, skuId={}, quantity={}", userId, skuId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        OmsCart cart = getById(cartId);
        if (cart != null && cart.getUserId().equals(userId)) {
            cart.setQuantity(quantity);
            updateById(cart);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSelected(Long userId, Long cartId, Integer selected) {
        OmsCart cart = getById(cartId);
        if (cart != null && cart.getUserId().equals(userId)) {
            cart.setSelected(selected);
            updateById(cart);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAll(Long userId, Integer selected) {
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCart::getUserId, userId);
        list(wrapper).forEach(cart -> {
            cart.setSelected(selected);
            updateById(cart);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(Long userId, Long cartId) {
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCart::getId, cartId);
        wrapper.eq(OmsCart::getUserId, userId);
        remove(wrapper);
    }

    @Override
    public Integer getCartCount(Long userId) {
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsCart::getUserId, userId);
        return (int) count(wrapper);
    }
}
