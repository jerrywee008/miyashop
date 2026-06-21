package com.miya.service.impl.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.pms.PmsProduct;
import com.miya.common.entity.pms.PmsSku;
import com.miya.common.entity.vo.SkuVO;
import com.miya.mapper.pms.PmsProductMapper;
import com.miya.mapper.pms.PmsSkuMapper;
import com.miya.service.pms.SkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SKU服务实现
 */
@Slf4j
@Service
public class SkuServiceImpl extends ServiceImpl<PmsSkuMapper, PmsSku> implements SkuService {

    @Autowired
    private PmsProductMapper productMapper;

    @Override
    public Page<SkuVO> pageList(Integer page, Integer size, Long productId, Integer status) {
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(PmsSku::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(PmsSku::getStatus, status);
        }
        wrapper.orderByAsc(PmsSku::getProductId);
        wrapper.orderByAsc(PmsSku::getId);

        Page<PmsSku> skuPage = page(new Page<>(page, size), wrapper);

        // 批量查商品名称
        List<Long> productIds = skuPage.getRecords().stream()
                .map(PmsSku::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> productNameMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(PmsProduct::getId, PmsProduct::getName, (a, b) -> a));

        // 转换为 VO
        List<SkuVO> voList = skuPage.getRecords().stream().map(sku -> {
            SkuVO vo = new SkuVO();
            vo.setId(sku.getId());
            vo.setProductId(sku.getProductId());
            vo.setSpuId(sku.getSpuId());
            vo.setImage(sku.getImage());
            vo.setPrice(sku.getPrice());
            vo.setStock(sku.getStock());
            vo.setSpecs(sku.getSpecs());
            vo.setStatus(sku.getStatus());

            // 组合显示名称：商品名 + 规格值
            String productName = productNameMap.getOrDefault(sku.getProductId(), "未知商品");
            String specsDisplay = formatSpecs(sku.getSpecs());
            vo.setName(specsDisplay.isEmpty() ? productName : productName + " - " + specsDisplay);

            return vo;
        }).collect(Collectors.toList());

        Page<SkuVO> voPage = new Page<>(page, size, skuPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 将 specs JSON 转为可读字符串，如 "红色 S码"
     */
    private String formatSpecs(String specs) {
        if (specs == null || specs.isBlank()) {
            return "";
        }
        try {
            // specs 格式: {"颜色":"红色","尺码":"S"}
            String content = specs.trim();
            if (content.startsWith("{") && content.endsWith("}")) {
                content = content.substring(1, content.length() - 1);
                StringBuilder sb = new StringBuilder();
                for (String pair : content.split(",")) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String value = kv[1].trim().replaceAll("^\"|\"$", "");
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(value);
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("解析specs失败: {}", specs, e);
        }
        return specs;
    }
}
