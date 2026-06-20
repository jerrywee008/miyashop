package com.miya.service.impl.sms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.sms.*;
import com.miya.common.constant.ResultCode;
import com.miya.common.exception.BusinessException;
import com.miya.mapper.sms.*;
import com.miya.service.sms.GroupBuyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 团购服务实现
 */
@Slf4j
@Service
public class GroupBuyServiceImpl extends ServiceImpl<SmsGroupbuyActivityMapper, SmsGroupbuyActivity> implements GroupBuyService {

    @Autowired
    private SmsGroupbuyActivityMapper activityMapper;
    @Autowired
    private SmsGroupbuyTeamMapper teamMapper;
    @Autowired
    private SmsGroupbuyMemberMapper memberMapper;

    @Override
    public Page<SmsGroupbuyActivity> pageActivities(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<SmsGroupbuyActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SmsGroupbuyActivity::getStatus, status);
        }
        wrapper.orderByDesc(SmsGroupbuyActivity::getSort);
        wrapper.orderByDesc(SmsGroupbuyActivity::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTeam(Long userId, Long activityId, Long skuId) {
        // 1. 检查活动
        SmsGroupbuyActivity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.GROUPBUY_NOT_STARTED);
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (now.compareTo(activity.getStartTime()) < 0) {
            throw new BusinessException(ResultCode.GROUPBUY_NOT_STARTED);
        }
        if (now.compareTo(activity.getEndTime()) > 0) {
            throw new BusinessException(ResultCode.GROUPBUY_ENDED);
        }

        // 2. 检查是否已有进行中的队伍
        LambdaQueryWrapper<SmsGroupbuyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SmsGroupbuyMember::getUserId, userId);

        // 3. 创建队伍
        int validHours = activity.getValidHours() != null ? activity.getValidHours() : 24;
        String expireTime = LocalDateTime.now().plusHours(validHours)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        SmsGroupbuyTeam team = new SmsGroupbuyTeam();
        team.setActivityId(activityId);
        team.setSkuId(skuId);
        team.setLeaderId(userId);
        team.setCurrentPeople(1);
        team.setMaxPeople(activity.getMaxPeople() != null ? activity.getMaxPeople() : activity.getMinPeople());
        team.setStatus(0); // 待成团
        team.setExpireTime(expireTime);
        teamMapper.insert(team);

        // 4. 添加团长为成员
        SmsGroupbuyMember member = new SmsGroupbuyMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setIsLeader(1);
        member.setJoinTime(now);
        memberMapper.insert(member);

        log.info("团购队伍创建: teamId={}, activityId={}, leaderId={}", team.getId(), activityId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("teamId", String.valueOf(team.getId()));
        result.put("expireTime", expireTime);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinTeam(Long userId, Long teamId) {
        // 1. 检查队伍
        SmsGroupbuyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团购队伍不存在");
        }
        if (team.getStatus() == 2) {
            throw new BusinessException(ResultCode.GROUPBUY_TEAM_FULL);
        }
        if (team.getStatus() == 3) {
            throw new BusinessException(ResultCode.GROUPBUY_TEAM_FAILED);
        }

        // 2. 检查是否已过期
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (now.compareTo(team.getExpireTime()) > 0) {
            team.setStatus(3); // 失败
            teamMapper.updateById(team);
            throw new BusinessException(ResultCode.GROUPBUY_TEAM_EXPIRED);
        }

        // 3. 检查是否已加入
        LambdaQueryWrapper<SmsGroupbuyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SmsGroupbuyMember::getTeamId, teamId);
        memberWrapper.eq(SmsGroupbuyMember::getUserId, userId);
        if (memberMapper.selectCount(memberWrapper) > 0) {
            throw new BusinessException("您已加入该队伍");
        }

        // 4. 检查是否已满
        if (team.getCurrentPeople() >= team.getMaxPeople()) {
            throw new BusinessException(ResultCode.GROUPBUY_TEAM_FULL);
        }

        // 5. 加入队伍
        SmsGroupbuyMember member = new SmsGroupbuyMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setIsLeader(0);
        member.setJoinTime(now);
        memberMapper.insert(member);

        // 6. 更新队伍人数
        team.setCurrentPeople(team.getCurrentPeople() + 1);

        // 7. 检查是否成团
        SmsGroupbuyActivity activity = activityMapper.selectById(team.getActivityId());
        if (activity != null && team.getCurrentPeople() >= activity.getMinPeople()) {
            team.setStatus(2); // 成功
            team.setSuccessTime(now);
        }
        teamMapper.updateById(team);

        log.info("加入团购队伍: teamId={}, userId={}, currentPeople={}", teamId, userId, team.getCurrentPeople());
    }

    @Override
    public SmsGroupbuyTeam getTeamDetail(Long teamId) {
        return teamMapper.selectById(teamId);
    }

    @Override
    public Page<SmsGroupbuyTeam> getTeamList(Long activityId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<SmsGroupbuyTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsGroupbuyTeam::getActivityId, activityId);
        if (status != null) {
            wrapper.eq(SmsGroupbuyTeam::getStatus, status);
        }
        wrapper.orderByDesc(SmsGroupbuyTeam::getCreatedTime);
        return teamMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredTeams() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LambdaQueryWrapper<SmsGroupbuyTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SmsGroupbuyTeam::getExpireTime, now);
        wrapper.ne(SmsGroupbuyTeam::getStatus, 3); // 排除已失败的
        wrapper.ne(SmsGroupbuyTeam::getStatus, 2); // 排除已成功的

        List<SmsGroupbuyTeam> expiredTeams = teamMapper.selectList(wrapper);
        for (SmsGroupbuyTeam team : expiredTeams) {
            team.setStatus(3); // 标记为失败
            teamMapper.updateById(team);
            log.info("团购队伍过期处理: teamId={}", team.getId());
        }
    }
}
