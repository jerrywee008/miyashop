package com.miya.web.controller.sms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.sms.SmsSeckillActivity;
import com.miya.common.entity.sms.SmsSeckillSku;
import com.miya.common.result.Result;
import com.miya.mapper.sms.SmsSeckillSkuMapper;
import com.miya.service.sms.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 秒杀控制器
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
@Tag(name = "秒杀管理")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;
    @Autowired
    private SmsSeckillSkuMapper seckillSkuMapper;

    @GetMapping("/activities")
    @Operation(summary = "秒杀活动列表")
    public Result<Page<SmsSeckillActivity>> getActivities(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(seckillService.pageActivities(page, size, status));
    }

    @GetMapping("/activity/{id}")
    @Operation(summary = "秒杀活动详情")
    public Result<SmsSeckillActivity> getActivityDetail(@PathVariable Long id) {
        return Result.success(seckillService.getById(id));
    }

    @PostMapping("/activity")
    @Operation(summary = "创建秒杀活动")
    public Result<Void> createActivity(@RequestBody SmsSeckillActivity activity) {
        seckillService.save(activity);
        return Result.success();
    }

    @PutMapping("/activity/{id}")
    @Operation(summary = "修改秒杀活动")
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody SmsSeckillActivity activity) {
        activity.setId(id);
        seckillService.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/activity/{id}")
    @Operation(summary = "删除秒杀活动")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        seckillService.removeById(id);
        return Result.success();
    }

    @GetMapping("/activity/{activityId}/skus")
    @Operation(summary = "秒杀活动商品列表")
    public Result<List<SmsSeckillSku>> getSkus(@PathVariable Long activityId) {
        List<SmsSeckillSku> skus = seckillSkuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsSeckillSku>()
                        .eq(SmsSeckillSku::getActivityId, activityId)
        );
        return Result.success(skus);
    }

    @PostMapping("/activity/{activityId}/skus")
    @Operation(summary = "添加秒杀商品")
    public Result<Void> addSku(@PathVariable Long activityId, @RequestBody SmsSeckillSku sku) {
        sku.setActivityId(activityId);
        seckillSkuMapper.insert(sku);
        return Result.success();
    }

    @DeleteMapping("/activity/{activityId}/skus/{skuId}")
    @Operation(summary = "删除秒杀商品")
    public Result<Void> deleteSku(@PathVariable Long activityId, @PathVariable Long skuId) {
        seckillSkuMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsSeckillSku>()
                        .eq(SmsSeckillSku::getActivityId, activityId)
                        .eq(SmsSeckillSku::getSkuId, skuId)
        );
        return Result.success();
    }

    @PostMapping("/order")
    @Operation(summary = "秒杀下单")
    public Result<Map<String, Object>> seckillOrder(@RequestBody Map<String, Object> request) {
        Long userId = 1L; // TODO: 从Token获取
        Long activityId = Long.valueOf(request.get("activityId").toString());
        Long skuId = Long.valueOf(request.get("skuId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        return Result.success(seckillService.seckillOrder(userId, activityId, skuId, quantity));
    }
}
