package com.miya.web.controller.ums;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.ums.UmsAddress;
import com.miya.common.entity.ums.UmsMember;
import com.miya.common.entity.ums.UmsMemberCoupon;
import com.miya.common.result.Result;
import com.miya.service.ums.MemberService;
import com.miya.mapper.ums.UmsAddressMapper;
import com.miya.mapper.ums.UmsMemberCouponMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员控制器
 */
@Slf4j
@RestController
@RequestMapping("/member")
@Tag(name = "会员管理")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private UmsAddressMapper addressMapper;
    @Autowired
    private UmsMemberCouponMapper memberCouponMapper;

    @PostMapping("/login")
    @Operation(summary = "用户登录/注册")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String mobile = params.get("mobile");
        String code = params.getOrDefault("code", "888888");
        return Result.success(memberService.login(mobile, code));
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<UmsMember> info(@Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        if (userId == null)
            userId = 1L;
        return Result.success(memberService.getById(userId));
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateInfo(@RequestBody UmsMember member) {
        memberService.updateById(member);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "用户列表(管理端)")
    public Result<Page<UmsMember>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "手机号") @RequestParam(required = false) String mobile,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(memberService.pageList(page, size, keyword, mobile, status));
    }

    // ========== 地址管理 ==========

    @GetMapping("/address/list")
    @Operation(summary = "获取地址列表")
    public Result<List<UmsAddress>> addressList(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        if (userId == null)
            userId = 1L;
        List<UmsAddress> list = addressMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UmsAddress>()
                        .eq(UmsAddress::getUserId, userId)
                        .orderByDesc(UmsAddress::getIsDefault));
        return Result.success(list);
    }

    @PostMapping("/address")
    @Operation(summary = "新增地址")
    public Result<Void> addAddress(@RequestBody UmsAddress address) {
        if (address.getUserId() == null)
            address.setUserId(1L);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 取消其他默认地址
            List<UmsAddress> list = addressMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UmsAddress>()
                            .eq(UmsAddress::getUserId, address.getUserId())
                            .eq(UmsAddress::getIsDefault, 1));
            list.forEach(a -> {
                a.setIsDefault(0);
                addressMapper.updateById(a);
            });
        }
        addressMapper.insert(address);
        return Result.success();
    }

    @PutMapping("/address/{id}")
    @Operation(summary = "修改地址")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UmsAddress address) {
        address.setId(id);
        addressMapper.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    @Operation(summary = "删除地址")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    @Operation(summary = "设置默认地址")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        UmsAddress address = addressMapper.selectById(id);
        if (address != null) {
            // 取消其他默认
            List<UmsAddress> list = addressMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UmsAddress>()
                            .eq(UmsAddress::getUserId, address.getUserId())
                            .eq(UmsAddress::getIsDefault, 1));
            list.forEach(a -> {
                a.setIsDefault(0);
                addressMapper.updateById(a);
            });
            address.setIsDefault(1);
            addressMapper.updateById(address);
        }
        return Result.success();
    }

    // ========== 优惠券 ==========

    @GetMapping("/coupons")
    @Operation(summary = "获取优惠券列表")
    public Result<List<UmsMemberCoupon>> couponList(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        if (userId == null)
            userId = 1L;
        List<UmsMemberCoupon> list = memberCouponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UmsMemberCoupon>()
                        .eq(UmsMemberCoupon::getMemberId, userId)
                        .orderByDesc(UmsMemberCoupon::getCreatedTime));
        return Result.success(list);
    }

    // ========== 短信验证码 ==========

    @PostMapping("/send-sms")
    @Operation(summary = "发送短信验证码")
    public Result<String> sendSms(@Parameter(description = "手机号") @RequestParam String mobile) {
        // TODO: 对接真实短信服务
        log.info("发送短信验证码到: {}", mobile);
        return Result.success("验证码已发送");
    }

    // ========== 收藏 ==========

    @GetMapping("/favorites")
    @Operation(summary = "获取收藏列表")
    public Result<java.util.List<Map<String, Object>>> favorites(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        if (userId == null)
            userId = 1L;
        // TODO: 对接收藏表查询
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("id", 1L);
        item.put("productId", 1L);
        item.put("name", "优雅碎花连衣裙");
        item.put("price", 299.00);
        item.put("originalPrice", 599.00);
        item.put("image", "/api/product/1/image");
        list.add(item);
        return Result.success(list);
    }

    @PostMapping("/favorites")
    @Operation(summary = "添加收藏")
    public Result<Void> addFavorite(@RequestBody Map<String, Object> params) {
        Long userId = 1L;
        Long productId = Long.valueOf(params.get("productId").toString());
        log.info("添加收藏: userId={}, productId={}", userId, productId);
        return Result.success();
    }

    @DeleteMapping("/favorites/{productId}")
    @Operation(summary = "取消收藏")
    public Result<Void> removeFavorite(@PathVariable Long productId) {
        Long userId = 1L;
        log.info("取消收藏: userId={}, productId={}", userId, productId);
        return Result.success();
    }
}
