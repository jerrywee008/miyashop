package com.miya.web.task;

import com.miya.service.sms.GroupBuyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 团购定时任务
 * 定期检查并处理过期的团购队伍
 */
@Slf4j
@Component
public class GroupBuyScheduler {

    @Autowired
    private GroupBuyService groupBuyService;

    /**
     * 每5分钟检查一次过期队伍
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void handleExpiredTeams() {
        log.info("开始处理过期团购队伍...");
        try {
            groupBuyService.handleExpiredTeams();
        } catch (Exception e) {
            log.error("处理过期团购队伍异常: ", e);
        }
        log.info("过期团购队伍处理完成");
    }
}
