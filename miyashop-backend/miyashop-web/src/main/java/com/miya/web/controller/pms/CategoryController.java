package com.miya.web.controller.pms;

import com.miya.common.entity.pms.PmsCategory;
import com.miya.common.result.Result;
import com.miya.service.pms.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/category")
@Tag(name = "商品分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<PmsCategory>> getTree() {
        return Result.success(categoryService.getTree());
    }

    @GetMapping("/list")
    @Operation(summary = "获取分类列表")
    public Result<List<PmsCategory>> list(
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "层级") @RequestParam(required = false) Integer level) {
        return Result.success(categoryService.getList(parentId, level));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<PmsCategory> detail(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result<Void> add(@RequestBody PmsCategory category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改分类")
    public Result<Void> update(@PathVariable Long id, @RequestBody PmsCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新分类显示状态")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @Parameter(description = "显示状态 0隐藏 1显示") @RequestParam Integer showStatus) {
        PmsCategory category = categoryService.getById(id);
        if (category != null) {
            category.setShowStatus(showStatus);
            categoryService.updateById(category);
        }
        return Result.success();
    }
}
