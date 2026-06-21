package com.miya.web.controller.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.vo.SkuVO;
import com.miya.common.result.Result;
import com.miya.service.pms.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SKU管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/sku")
@Tag(name = "SKU管理")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @GetMapping("/list")
    @Operation(summary = "SKU列表")
    public Result<Page<SkuVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "商品ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(skuService.pageList(page, size, productId, status));
    }
}
