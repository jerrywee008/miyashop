package com.miya.service.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.pms.PmsProduct;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<PmsProduct> {

    /**
     * 分页查询商品列表
     */
    Page<PmsProduct> pageList(Integer page, Integer size, String name, Long categoryId, Integer status);

    /**
     * 上架商品
     */
    void onShelf(Long id);

    /**
     * 下架商品
     */
    void offShelf(Long id);

    /**
     * 减库存
     */
    boolean deductStock(Long productId, Integer quantity);

    /**
     * 增加销量
     */
    void increaseSales(Long productId, Integer quantity);
}
