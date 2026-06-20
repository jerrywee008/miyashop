package com.miya.web.controller.sms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miya.common.entity.sms.SmsGroupbuyActivity;
import com.miya.common.entity.sms.SmsGroupbuyTeam;
import com.miya.common.result.Result;
import com.miya.service.sms.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 团购控制器
 */
@Slf4j
@RestController
@RequestMapping("/groupbuy")
@Tag(name = "团购管理")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    @GetMapping("/activities")
    @Operation(summary = "团购活动列表")
    public Result<Page<SmsGroupbuyActivity>> getActivities(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(groupBuyService.pageActivities(page, size, status));
    }

    @GetMapping("/activity/{id}")
    @Operation(summary = "团购活动详情")
    public Result<SmsGroupbuyActivity> getActivityDetail(@PathVariable Long id) {
        return Result.success(groupBuyService.getById(id));
    }

    @PostMapping("/activity")
    @Operation(summary = "创建团购活动")
    public Result<Void> createActivity(@RequestBody SmsGroupbuyActivity activity) {
        groupBuyService.save(activity);
        return Result.success();
    }

    @PutMapping("/activity/{id}")
    @Operation(summary = "修改团购活动")
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody SmsGroupbuyActivity activity) {
        activity.setId(id);
        groupBuyService.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/activity/{id}")
    @Operation(summary = "删除团购活动")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        groupBuyService.removeById(id);
        return Result.success();
    }

    @GetMapping("/activity/{activityId}/teams")
    @Operation(summary = "活动队伍列表")
    public Result<Page<SmsGroupbuyTeam>> getTeamList(
            @PathVariable Long activityId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(groupBuyService.getTeamList(activityId, page, size, status));
    }

    @GetMapping("/team/{id}")
    @Operation(summary = "团购队伍详情")
    public Result<SmsGroupbuyTeam> getTeamDetail(@PathVariable Long id) {
        return Result.success(groupBuyService.getTeamDetail(id));
    }

    @PostMapping("/team/create")
    @Operation(summary = "创建团购队伍")
    public Result<Map<String, Object>> createTeam(@RequestBody Map<String, Object> request) {
        Long userId = 1L; // TODO: 从Token获取
        Long activityId = Long.valueOf(request.get("activityId").toString());
        Long skuId = Long.valueOf(request.get("skuId").toString());
        return Result.success(groupBuyService.createTeam(userId, activityId, skuId));
    }

    @PostMapping("/team/{id}/join")
    @Operation(summary = "加入团购队伍")
    public Result<Void> joinTeam(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> request) {
        Long userId = 1L; // TODO: 从Token获取
        groupBuyService.joinTeam(userId, id);
        return Result.success();
    }
}
