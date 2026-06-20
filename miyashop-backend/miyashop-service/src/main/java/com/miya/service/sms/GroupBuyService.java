package com.miya.service.sms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.sms.SmsGroupbuyActivity;
import com.miya.common.entity.sms.SmsGroupbuyTeam;

import java.util.Map;

/**
 * 团购服务接口
 */
public interface GroupBuyService extends IService<SmsGroupbuyActivity> {

    /**
     * 获取团购活动列表
     */
    Page<SmsGroupbuyActivity> pageActivities(Integer page, Integer size, Integer status);

    /**
     * 创建团购队伍
     */
    Map<String, Object> createTeam(Long userId, Long activityId, Long skuId);

    /**
     * 加入团购队伍
     */
    void joinTeam(Long userId, Long teamId);

    /**
     * 获取队伍详情
     */
    SmsGroupbuyTeam getTeamDetail(Long teamId);

    /**
     * 获取活动队伍列表
     */
    Page<SmsGroupbuyTeam> getTeamList(Long activityId, Integer page, Integer size, Integer status);

    /**
     * 处理过期队伍
     */
    void handleExpiredTeams();
}
