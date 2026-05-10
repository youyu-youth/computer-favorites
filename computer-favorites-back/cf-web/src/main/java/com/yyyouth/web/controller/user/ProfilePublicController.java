package com.yyyouth.web.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.vo.userstats.CategoryDistributionVO;
import com.yyyouth.model.vo.userstats.ContributionGraphVO;
import com.yyyouth.model.vo.userstats.DashboardOverviewVO;
import com.yyyouth.model.vo.userstats.ProfilePublicVO;
import com.yyyouth.model.vo.userstats.TechRadarVO;
import com.yyyouth.model.vo.userstats.TrendSeriesVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.service.audit.annotation.AuditLog;
import com.yyyouth.service.ratelimit.annotation.RateLimit;
import com.yyyouth.service.user.userstats.ProfileDashboardService;
import com.yyyouth.service.user.userstats.ProfilePublicService;
import com.yyyouth.service.user.userstats.ProfilePublicService.ProfileVisibilityCheckResult;
import com.yyyouth.service.user.userstats.UploadImpactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Locale;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 公开主页接口（user-15 M5）。
 *
 * 不强制登录：进入控制器时不加 @SaCheckLogin，由 ProfilePublicService 按 visibility 决定 401/403。
 *
 * 提供 2 个端点：
 *  1. GET /api/user/profile/public/{username} → 基础公开资料
 *  2. GET /api/user/profile/public/{username}/dashboard/{section} → 公开看板（5 选 1）
 */
@Slf4j
@Api(tags = "公开主页接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/profile/public")
@AuditLog(module = "user-profile-dashboard")
public class ProfilePublicController {

    /** 看板 section 名称 */
    private static final String SECTION_OVERVIEW = "overview";
    private static final String SECTION_GRAPH = "contribution-graph";
    private static final String SECTION_CATEGORY = "category-distribution";
    private static final String SECTION_RADAR = "tech-radar";
    private static final String SECTION_TREND = "trend-series";
    private static final String SECTION_UPLOAD_IMPACT = "upload-impact";

    private final ProfilePublicService profilePublicService;
    private final ProfileDashboardService profileDashboardService;
    private final UploadImpactService uploadImpactService;

    /**
     * 公开主页基础资料。
     */
    @ApiOperation(value = "公开主页基础资料")
    @GetMapping("/{username}")
    @AuditLog(action = "public-profile", description = "查询公开主页基础资料，username=#{#username}")
    public HttpResult getPublicProfile(@PathVariable("username") @NotBlank String username) {
        Long currentUserId = currentUserIdOrNull();
        log.info("[public-profile] viewer={} target={}", currentUserId, username);
        ProfilePublicVO vo = profilePublicService.getPublicProfile(username, currentUserId);
        return HttpResult.success(vo);
    }

    /**
     * 公开主页看板某一 section。
     * section 取值：overview / contribution-graph / category-distribution / tech-radar / trend-series。
     *
     * 该接口先按 username 解析 targetUserId 并复用 dashboard 看板服务。
     * 隐私校验在 ProfilePublicService.checkDashboardAccess 内完成。
     */
    @ApiOperation(value = "公开主页看板 section")
    @GetMapping("/{username}/dashboard/{section}")
    @RateLimit(key = "profile:dashboard:public:#{#username}:#{#section}:#{#loginId}", limit = 5, window = 1)
    @AuditLog(action = "public-dashboard", description = "查询公开主页看板，username=#{#username}，section=#{#section}")
    public HttpResult getPublicDashboardSection(
            @PathVariable("username") @NotBlank String username,
            @PathVariable("section") @NotBlank String section,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "range", required = false, defaultValue = "30d") String range,
            @RequestParam(value = "metric", required = false, defaultValue = "pv") String metric) {

        Long currentUserId = currentUserIdOrNull();
        // 先取目标 userId（公开 profile 内的 user.id），同时复用其隐私快照
        ProfilePublicVO publicVO = profilePublicService.getPublicProfile(username, currentUserId);
        Long targetUserId = publicVO.getUser().getId();

        String normalized = section == null ? "" : section.trim().toLowerCase(Locale.ROOT);
        boolean requireContribution = SECTION_OVERVIEW.equals(normalized)
                || SECTION_GRAPH.equals(normalized)
                || SECTION_TREND.equals(normalized);
        ProfileVisibilityCheckResult check = profilePublicService.checkDashboardAccess(
                targetUserId, currentUserId, requireContribution);

        log.info("[public-dashboard] viewer={} target={} section={} requireContribution={}",
                currentUserId, targetUserId, normalized, requireContribution);

        return switch (normalized) {
            case SECTION_OVERVIEW -> {
                DashboardOverviewVO data = profileDashboardService.getOverview(targetUserId);
                yield HttpResult.success(data);
            }
            case SECTION_GRAPH -> {
                int y = year == null ? LocalDate.now().getYear() : year;
                ContributionGraphVO data = profileDashboardService.getContributionGraph(targetUserId, y);
                yield HttpResult.success(data);
            }
            case SECTION_CATEGORY -> {
                CategoryDistributionVO data = profileDashboardService.getCategoryDistribution(targetUserId);
                yield HttpResult.success(data);
            }
            case SECTION_RADAR -> {
                TechRadarVO data = profileDashboardService.getTechRadar(targetUserId);
                yield HttpResult.success(data);
            }
            case SECTION_TREND -> {
                TrendSeriesVO data = profileDashboardService.getTrendSeries(targetUserId, range, metric);
                yield HttpResult.success(data);
            }
            case SECTION_UPLOAD_IMPACT -> {
                UploadImpactVO data = uploadImpactService.getUploadImpact(targetUserId, range);
                yield HttpResult.success(data);
            }
            default -> HttpResult.error(400, "未知 section：" + section + "（支持 overview / contribution-graph / category-distribution / tech-radar / trend-series / upload-impact）");
        };
    }

    /**
     * 当前登录者 ID；未登录返回 null（不抛异常）。
     */
    private Long currentUserIdOrNull() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
