package com.miya.web.controller.marketing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.pms.PmsCoupon;
import com.miya.common.result.Result;
import com.miya.mapper.pms.PmsCouponMapper;
import com.miya.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券管理")
public class CouponController {

    @Autowired
    private CouponService couponService;
    @Autowired
    private PmsCouponMapper couponMapper;

    @GetMapping("/list")
    @Operation(summary = "优惠券列表")
    public Result<Page<PmsCoupon>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "优惠券名称") @RequestParam(required = false) String name,
            @Parameter(description = "类型 1满减 2折扣") @RequestParam(required = false) Integer type,
            @Parameter(description = "状态 0禁用 1启用") @RequestParam(required = false) Integer status) {
        return Result.success(couponService.pageList(page, size, name, type, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "优惠券详情")
    public Result<PmsCoupon> detail(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @Transactional
    @PostMapping
    @Operation(summary = "创建优惠券")
    public Result<Void> create(@RequestBody PmsCoupon coupon) {
        if (isNameDuplicate(coupon.getName(), null)) {
            return Result.failed("优惠券名称已存在，请更换名称");
        }
        coupon.setDeleted(0);
        couponService.save(coupon);
        return Result.success();
    }

    @Transactional
    @PutMapping("/{id}")
    @Operation(summary = "更新优惠券")
    public Result<Void> update(@PathVariable Long id, @RequestBody PmsCoupon coupon) {
        if (isNameDuplicate(coupon.getName(), id)) {
            return Result.failed("优惠券名称已存在，请更换名称");
        }
        coupon.setId(id);
        couponService.updateById(coupon);
        return Result.success();
    }

    private boolean isNameDuplicate(String name, Long excludeId) {
        // 直接查数据库，绕过 @TableLogic 的 deleted=0 默认条件
        LambdaQueryWrapper<PmsCoupon> wrapper = new LambdaQueryWrapper<PmsCoupon>()
                .eq(PmsCoupon::getName, name);
        if (excludeId != null) {
            wrapper.ne(PmsCoupon::getId, excludeId);
        }
        return couponMapper.selectCount(wrapper) > 0;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新优惠券状态")
    public Result<Void> updateStatus(@PathVariable Long id,
                                      @Parameter(description = "状态") @RequestParam Integer status) {
        PmsCoupon coupon = new PmsCoupon();
        coupon.setId(id);
        coupon.setStatus(status);
        couponService.updateById(coupon);
        return Result.success();
    }
}
