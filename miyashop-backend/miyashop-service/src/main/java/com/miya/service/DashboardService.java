package com.miya.service;

import com.miya.common.entity.vo.DashboardVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取首页仪表盘数据
     */
    DashboardVO getDashboard();
}
