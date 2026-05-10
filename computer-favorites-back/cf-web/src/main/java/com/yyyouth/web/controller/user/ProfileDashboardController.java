package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.vo.userstats.CategoryDistributionVO;
import com.yyyouth.model.vo.userstats.ContributionGraphVO;
import com.yyyouth.model.vo.userstats.DashboardOverviewVO;
import com.yyyouth.model.vo.userstats.TechRadarVO;
import com.yyyouth.model.vo.userstats.TrendSeriesVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.service.audit.annotation.AuditLog;
import com.yyyouth.service.ratelimit.annotation.RateLimit;
import com.yyyouth.service.user.userstats.ProfileDashboardService;
import com.yyyouth.service.user.userstats.UploadImpactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页看板接口控制器 - user-15
 *
 * 5 个细粒度 GET 端点，前端可并行拉取，单接口失败不影响其他卡片。
 * 全部读路径走 Cache-Aside，单点失败时降级直查 DB。
 */
@Slf4j
@Api(tags = "用户主页看板接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/profile/dashboard")
@AuditLog(module = "user-profile-dashboard")
public class ProfileDashboardController {

    private final ProfileDashboardService profileDashboardService;

    private final UploadImpactService uploadImpactService;

    /**
     * 贡献概览（6 累计 + 等级 + streak + rank_percent + lastActiveDate）
     */
    @ApiOperation(value = "贡献概览")
    @GetMapping("/overview")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "overview", description = "查询用户主页贡献概览")
    public HttpResult overview() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("[dashboard] overview userId={}", userId);
        return HttpResult.success(profileDashboardService.getOverview(userId));
    }

    /**
     * 贡献热力图
     *
     * @param year 年份；缺省取当前年
     */
    @ApiOperation(value = "贡献热力图")
    @GetMapping("/contribution-graph")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "contribution-graph", description = "查询用户主页贡献热力图，year=#{#year}")
    public HttpResult contributionGraph(
            @RequestParam(value = "year", required = false) Integer year) {
        Long userId = StpUtil.getLoginIdAsLong();
        int y = year == null ? LocalDate.now().getYear() : year;
        log.info("[dashboard] contributionGraph userId={}, year={}", userId, y);
        return HttpResult.success(profileDashboardService.getContributionGraph(userId, y));
    }

    /**
     * 分类偏好分布（TOP8 + 其他）
     */
    @ApiOperation(value = "分类偏好分布")
    @GetMapping("/category-distribution")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "category-distribution", description = "查询用户主页分类偏好分布")
    public HttpResult categoryDistribution() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("[dashboard] categoryDistribution userId={}", userId);
        return HttpResult.success(profileDashboardService.getCategoryDistribution(userId));
    }

    /**
     * 技术雷达（6 维 + 语言占比）
     */
    @ApiOperation(value = "技术雷达")
    @GetMapping("/tech-radar")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "tech-radar", description = "查询用户主页技术雷达")
    public HttpResult techRadar() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("[dashboard] techRadar userId={}", userId);
        return HttpResult.success(profileDashboardService.getTechRadar(userId));
    }

    /**
     * 趋势折线
     *
     * @param range  时间范围：7d / 30d / 90d，默认 30d
     * @param metric 指标：pv / likes / favorites / comments / contribution，默认 pv
     */
    @ApiOperation(value = "趋势折线")
    @GetMapping("/trend-series")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "trend-series", description = "查询用户主页趋势折线，range=#{#range}，metric=#{#metric}")
    public HttpResult trendSeries(
            @RequestParam(value = "range", required = false, defaultValue = "30d") String range,
            @RequestParam(value = "metric", required = false, defaultValue = "pv") String metric) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("[dashboard] trendSeries userId={}, range={}, metric={}", userId, range, metric);
        return HttpResult.success(profileDashboardService.getTrendSeries(userId, range, metric));
    }

    /**
     * 上传网站影响力：聚合「他人对当前用户上传网站」的浏览/点赞/收藏/评论/评分。
     *
     * @param range 时间范围：7d / 30d / 90d / all，默认 30d
     */
    @ApiOperation(value = "上传网站影响力")
    @GetMapping("/upload-impact")
    @SaCheckLogin
    @RateLimit(key = "profile:dashboard:#{#loginId}", limit = 30, window = 1)
    @AuditLog(action = "upload-impact", description = "查询上传网站影响力，range=#{#range}")
    public HttpResult uploadImpact(
            @RequestParam(value = "range", required = false, defaultValue = "30d") String range) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("[dashboard] uploadImpact userId={}, range={}", userId, range);
        UploadImpactVO data = uploadImpactService.getUploadImpact(userId, range);
        return HttpResult.success(data);
    }
}
