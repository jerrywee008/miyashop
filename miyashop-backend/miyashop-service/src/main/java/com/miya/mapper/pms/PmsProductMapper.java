package com.miya.mapper.pms;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miya.common.entity.pms.PmsProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper
 */
@Mapper
public interface PmsProductMapper extends BaseMapper<PmsProduct> {
}
