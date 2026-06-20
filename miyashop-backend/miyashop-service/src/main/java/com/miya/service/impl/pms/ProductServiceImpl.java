package com.miya.service.impl.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.pms.PmsProduct;
import com.miya.common.exception.BusinessException;
import com.miya.common.constant.ResultCode;
import com.miya.mapper.pms.PmsProductMapper;
import com.miya.service.pms.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<PmsProductMapper, PmsProduct> implements ProductService {

    @Override
    public Page<PmsProduct> pageList(Integer page, Integer size, String name, Long categoryId, Integer status) {
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(PmsProduct::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(PmsProduct::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(PmsProduct::getStatus, status);
        }
        wrapper.orderByDesc(PmsProduct::getSort);
        wrapper.orderByDesc(PmsProduct::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onShelf(Long id) {
        PmsProduct product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(1);
        updateById(product);
        log.info("商品上架: id={}, name={}", id, product.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(Long id) {
        PmsProduct product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        product.setStatus(0);
        updateById(product);
        log.info("商品下架: id={}, name={}", id, product.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long productId, Integer quantity) {
        PmsProduct product = getById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(ResultCode.PRODUCT_OUT_OF_STOCK);
        }
        product.setStock(product.getStock() - quantity);
        return updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseSales(Long productId, Integer quantity) {
        PmsProduct product = getById(productId);
        if (product != null) {
            product.setSales(product.getSales() + quantity);
            updateById(product);
        }
    }
}
