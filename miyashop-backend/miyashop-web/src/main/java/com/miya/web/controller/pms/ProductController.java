package com.miya.web.controller.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.pms.PmsProduct;
import com.miya.common.result.Result;
import com.miya.service.pms.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/product")
@Tag(name = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Result<Page<PmsProduct>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "商品名称") @RequestParam(required = false) String name,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(productService.pageList(page, size, name, categoryId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<PmsProduct> detail(@Parameter(description = "商品ID") @PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增商品")
    public Result<Void> add(@RequestBody PmsProduct product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改商品")
    public Result<Void> update(@PathVariable Long id, @RequestBody PmsProduct product) {
        product.setId(id);
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "商品上下架")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @Parameter(description = "状态 0下架 1上架") @RequestParam Integer status) {
        if (status == 1) {
            productService.onShelf(id);
        } else {
            productService.offShelf(id);
        }
        return Result.success();
    }
}
