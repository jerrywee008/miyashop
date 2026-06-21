package com.miya.service.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.pms.PmsSku;
import com.miya.common.entity.vo.SkuVO;

/**
 * SKU服务接口
 */
public interface SkuService extends IService<PmsSku> {

    /**
     * 分页查询SKU列表（含商品名称）
     *
     * @param page      页码
     * @param size      每页数量
     * @param productId 商品ID（可选）
     * @param status    状态（可选）
     * @return SKU视图分页
     */
    Page<SkuVO> pageList(Integer page, Integer size, Long productId, Integer status);
}
