package com.yyyouth.service.user.userstats;

import com.yyyouth.model.vo.userstats.CategoryDistributionVO;
import com.yyyouth.model.vo.userstats.ContributionGraphVO;
import com.yyyouth.model.vo.userstats.DashboardOverviewVO;
import com.yyyouth.model.vo.userstats.TechRadarVO;
import com.yyyouth.model.vo.userstats.TrendSeriesVO;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页看板服务 - user-15
 *
 * 读路径全部走 Cache-Aside；写路径由 MQ 消费端负责，本接口仅查询。
 */
public interface ProfileDashboardService {

    /**
     * 贡献概览（6 累计 + 等级 + streak + rank_percent + lastActiveDate）
     */
    DashboardOverviewVO getOverview(Long userId);

    /**
     * 贡献热力图（年）
     *
     * @param year 年份；超出 [2023, 当前年] 范围将抛出非法参数异常
     */
    ContributionGraphVO getContributionGraph(Long userId, int year);

    /**
     * 分类偏好分布（TOP8 + 其他）
     */
    CategoryDistributionVO getCategoryDistribution(Long userId);

    /**
     * 技术雷达（6 维能力 + 用户技术栈语言占比）
     */
    TechRadarVO getTechRadar(Long userId);

    /**
     * 趋势折线
     *
     * @param range  时间范围：7d / 30d / 90d
     * @param metric 指标：pv / likes / favorites / comments / contribution
     */
    TrendSeriesVO getTrendSeries(Long userId, String range, String metric);
}
