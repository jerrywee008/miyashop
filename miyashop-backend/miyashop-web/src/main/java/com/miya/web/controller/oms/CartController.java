package com.miya.web.controller.oms;

import com.miya.common.entity.oms.OmsCart;
import com.miya.common.result.Result;
import com.miya.service.oms.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车管理")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/list")
    @Operation(summary = "获取购物车列表")
    public Result<List<OmsCart>> list(@Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        if (userId == null) userId = 1L; // TODO: 从Token获取
        return Result.success(cartService.getUserCart(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "添加到购物车")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : 1L;
        Long productId = Long.valueOf(params.get("productId").toString());
        Long skuId = Long.valueOf(params.get("skuId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        cartService.addToCart(userId, productId, skuId, quantity);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新购物车商品数量")
    public Result<Void> updateQuantity(@PathVariable Long id,
                                        @RequestBody Map<String, Object> params) {
        Long userId = 1L; // TODO: 从Token获取
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        cartService.updateQuantity(userId, id, quantity);
        return Result.success();
    }

    @PutMapping("/{id}/selected")
    @Operation(summary = "更新选中状态")
    public Result<Void> updateSelected(@PathVariable Long id,
                                        @Parameter(description = "选中状态") @RequestParam Integer selected) {
        Long userId = 1L;
        cartService.updateSelected(userId, id, selected);
        return Result.success();
    }

    @PutMapping("/select-all")
    @Operation(summary = "全选/取消全选")
    public Result<Void> selectAll(@Parameter(description = "选中状态") @RequestParam Integer selected) {
        Long userId = 1L;
        cartService.selectAll(userId, selected);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车商品")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = 1L;
        cartService.removeItem(userId, id);
        return Result.success();
    }

    @GetMapping("/count")
    @Operation(summary = "获取购物车数量")
    public Result<Integer> count() {
        Long userId = 1L;
        return Result.success(cartService.getCartCount(userId));
    }
}
