package com.yyyouth.service.user.userstats.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.enums.UserActivityType;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserStatsDaily;
import com.yyyouth.model.pojo.user.UserStatsOverview;
import com.yyyouth.model.pojo.user.UserTagAffinity;
import com.yyyouth.model.pojo.user.TechStack;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.userstats.CategoryDistributionVO;
import com.yyyouth.model.vo.userstats.CategoryShareVO;
import com.yyyouth.model.vo.userstats.ContributionCellVO;
import com.yyyouth.model.vo.userstats.ContributionGraphVO;
import com.yyyouth.model.vo.userstats.ContributionMonthLabelVO;
import com.yyyouth.model.vo.userstats.DashboardOverviewVO;
import com.yyyouth.model.vo.userstats.TechLanguageVO;
import com.yyyouth.model.vo.userstats.TechRadarDimVO;
import com.yyyouth.model.vo.userstats.TechRadarVO;
import com.yyyouth.model.vo.userstats.TrendPointVO;
import com.yyyouth.model.vo.userstats.TrendSeriesVO;
import com.yyyouth.service.mapper.user.TechStackMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserStatsDailyMapper;
import com.yyyouth.service.mapper.user.UserStatsOverviewMapper;
import com.yyyouth.service.mapper.user.UserTagAffinityMapper;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.ProfileDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页看板服务实现 - user-15
 *
 * 全部读路径走 Cache-Aside（{@link RedisCache}）；写路径由 MQ 消费端在 {@code UserStatsAggregateServiceImpl} 处理。
 *
 * 兜底说明：在 M3 阶段，T+1 重算任务尚未上线，因此 overview 中的 levelCode / streakDays / maxStreakDays / rankPercent
 * 由本 Service 读时计算（轻量 SQL）。M4 任务上线后将由后台批量覆写到 t_user_stats_overview 字段，本类的兜底分支会自动让位
 * 给已存在的字段值。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDashboardServiceImpl implements ProfileDashboardService {

    private static final long TTL_OVERVIEW_SECONDS = 600L;
    private static final long TTL_GRAPH_SECONDS = 1800L;
    private static final long TTL_CATEGORY_SECONDS = 900L;
    private static final long TTL_RADAR_SECONDS = 1800L;
    private static final long TTL_TREND_SECONDS = 600L;

    private static final long JITTER_60_SECONDS = 60L;
    private static final long JITTER_120_SECONDS = 120L;

    /** 热力图最早可查询年份（避免无谓全表扫描） */
    private static final int GRAPH_MIN_YEAR = 2023;

    /** TOP-N 截取数量（分类偏好） */
    private static final int CATEGORY_TOP_N = 8;

    private static final int NOT_DELETED = 0;

    private static final int AUDIT_APPROVED_STATUS = 1;

    /** 维度：分类 */
    private static final int DIM_CATEGORY = 1;

    /** 6 个雷达维度（顺序固定，前端依此渲染） */
    private static final List<String> RADAR_DIMS = List.of("前端", "后端", "数据库", "AI", "工程化", "算法");

    /** 技术栈名 → 雷达维度名（关键词匹配，多对一） */
    private static final Map<String, String> TECH_TO_DIM;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        // 前端
        Arrays.asList("vue", "react", "angular", "svelte", "html", "css", "tailwind", "sass", "less",
                "javascript", "typescript", "jquery", "vite", "webpack", "next.js", "nuxt", "primevue", "echarts")
                .forEach(t -> map.put(t, "前端"));
        // 后端
        Arrays.asList("java", "spring", "spring boot", "spring cloud", "go", "golang", "node", "node.js",
                "nest", "express", "koa", "php", "ruby", "rails", "rust", "kotlin", "scala", "c#", ".net",
                "django", "flask", "fastapi")
                .forEach(t -> map.put(t, "后端"));
        // 数据库
        Arrays.asList("mysql", "postgresql", "postgres", "redis", "mongodb", "elasticsearch", "clickhouse",
                "oracle", "sqlite", "tidb", "doris", "hbase", "neo4j", "minio", "kafka", "rabbitmq", "dynamodb")
                .forEach(t -> map.put(t, "数据库"));
        // AI
        Arrays.asList("python", "tensorflow", "pytorch", "spring ai", "llm", "openai", "deepseek",
                "langchain", "transformers", "huggingface", "ai", "ml", "nlp", "cv")
                .forEach(t -> map.put(t, "AI"));
        // 工程化
        Arrays.asList("docker", "kubernetes", "k8s", "jenkins", "git", "github actions", "ci/cd",
                "linux", "nginx", "shell", "bash", "ansible", "terraform", "prometheus", "grafana")
                .forEach(t -> map.put(t, "工程化"));
        // 算法
        Arrays.asList("leetcode", "algorithm", "data structure", "动态规划", "dp", "graph", "tree", "binary search",
                "数据结构", "算法")
                .forEach(t -> map.put(t, "算法"));
        TECH_TO_DIM = Collections.unmodifiableMap(map);
    }

    private final UserStatsOverviewMapper userStatsOverviewMapper;
    private final UserStatsDailyMapper userStatsDailyMapper;
    private final UserTagAffinityMapper userTagAffinityMapper;
    private final UserProfileMapper userProfileMapper;
    private final CategoryMapper categoryMapper;
    private final WebsiteMapper websiteMapper;
    private final TechStackMapper techStackMapper;
    private final RedisCache redisCache;

    @Override
    public DashboardOverviewVO getOverview(Long userId) {
        validateUserId(userId);
        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":overview";
        return redisCache.getOrLoad(key, DashboardOverviewVO.class,
                TTL_OVERVIEW_SECONDS, JITTER_60_SECONDS,
                () -> loadOverview(userId));
    }

    @Override
    public ContributionGraphVO getContributionGraph(Long userId, int year) {
        validateUserId(userId);
        validateGraphYear(year);
        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":graph:" + year;
        return redisCache.getOrLoad(key, ContributionGraphVO.class,
                TTL_GRAPH_SECONDS, JITTER_120_SECONDS,
                () -> loadContributionGraph(userId, year));
    }

    @Override
    public CategoryDistributionVO getCategoryDistribution(Long userId) {
        validateUserId(userId);
        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":category";
        return redisCache.getOrLoad(key, CategoryDistributionVO.class,
                TTL_CATEGORY_SECONDS, JITTER_60_SECONDS,
                () -> loadCategoryDistribution(userId));
    }

    @Override
    public TechRadarVO getTechRadar(Long userId) {
        validateUserId(userId);
        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":radar";
        return redisCache.getOrLoad(key, TechRadarVO.class,
                TTL_RADAR_SECONDS, JITTER_120_SECONDS,
                () -> loadTechRadar(userId));
    }

    @Override
    public TrendSeriesVO getTrendSeries(Long userId, String range, String metric) {
        validateUserId(userId);
        int days = parseRangeDays(range);
        String column = parseMetricColumn(metric);
        String normalizedRange = days + "d";
        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId
                + ":trend:" + normalizedRange + ":" + metric;
        return redisCache.getOrLoad(key, TrendSeriesVO.class,
                TTL_TREND_SECONDS, JITTER_60_SECONDS,
                () -> loadTrendSeries(userId, days, normalizedRange, metric, column));
    }

    // -------------------- 回源实现 --------------------

    private DashboardOverviewVO loadOverview(Long userId) {
        UserStatsOverview overview = userStatsOverviewMapper.selectOne(
                new LambdaQueryWrapper<UserStatsOverview>()
                        .eq(UserStatsOverview::getUserId, userId)
                        .last("limit 1"));

        if (overview == null) {
            return DashboardOverviewVO.builder()
                    .totalSubmit(0).totalComment(0).totalCollect(0)
                    .totalLike(0).totalScore(0).totalBrowse(0)
                    .totalContribution(BigDecimal.ZERO)
                    .levelCode("C")
                    .streakDays(0).maxStreakDays(0)
                    .rankPercent(BigDecimal.ZERO)
                    .lastActiveDate(null)
                    .build();
        }

        // streak 兜底：T+1 任务未运行时，从 daily 表回溯计算
        int streak = overview.getStreakDays() != null ? overview.getStreakDays()
                : computeCurrentStreak(userId, overview.getLastActiveDate());
        int maxStreak = overview.getMaxStreakDays() != null ? overview.getMaxStreakDays()
                : Math.max(streak, 0);

        // levelCode 兜底：基于 totalContribution 阈值
        String levelCode = StringUtils.hasText(overview.getLevelCode())
                ? overview.getLevelCode()
                : computeLevelCode(overview.getTotalContribution());

        BigDecimal rankPercent = overview.getRankPercent() != null
                ? overview.getRankPercent()
                : BigDecimal.ZERO;

        return DashboardOverviewVO.builder()
                .totalSubmit(nz(overview.getTotalSubmit()))
                .totalComment(nz(overview.getTotalComment()))
                .totalCollect(nz(overview.getTotalCollect()))
                .totalLike(nz(overview.getTotalLike()))
                .totalScore(nz(overview.getTotalScore()))
                .totalBrowse(nz(overview.getTotalBrowse()))
                .totalContribution(overview.getTotalContribution() != null
                        ? overview.getTotalContribution() : BigDecimal.ZERO)
                .levelCode(levelCode)
                .streakDays(streak)
                .maxStreakDays(maxStreak)
                .rankPercent(rankPercent)
                .lastActiveDate(overview.getLastActiveDate())
                .build();
    }

    private ContributionGraphVO loadContributionGraph(Long userId, int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<UserStatsDaily> rows = userStatsDailyMapper.selectList(
                new LambdaQueryWrapper<UserStatsDaily>()
                        .eq(UserStatsDaily::getUserId, userId)
                        .between(UserStatsDaily::getStatDate, yearStart, yearEnd));

        Map<LocalDate, Integer> dayCount = new HashMap<>();
        int totalCount = 0;
        int maxDaily = 0;
        for (UserStatsDaily row : rows) {
            int sum = nz(row.getSubmitCount()) + nz(row.getCommentCount())
                    + nz(row.getCollectCount()) + nz(row.getLikeCount())
                    + nz(row.getScoreCount()) + nz(row.getBrowseCount());
            dayCount.put(row.getStatDate(), sum);
            totalCount += sum;
            if (sum > maxDaily) {
                maxDaily = sum;
            }
        }

        List<ContributionCellVO> cells = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate cursor = yearStart;
        while (!cursor.isAfter(yearEnd)) {
            int count = dayCount.getOrDefault(cursor, 0);
            cells.add(ContributionCellVO.builder()
                    .date(cursor.format(fmt))
                    .count(count)
                    .level(computeCellLevel(count, maxDaily))
                    .build());
            cursor = cursor.plusDays(1);
        }

        // 月份标签：每月第一个出现日所在 ISO 周列号作为 colOffset（与 yearStart 的 ISO 周差）
        WeekFields weekFields = WeekFields.ISO;
        List<ContributionMonthLabelVO> months = new ArrayList<>(12);
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        long startEpochWeek = epochWeek(yearStart, weekFields);
        for (int m = 1; m <= 12; m++) {
            LocalDate first = LocalDate.of(year, m, 1);
            int colOffset = (int) (epochWeek(first, weekFields) - startEpochWeek);
            months.add(ContributionMonthLabelVO.builder()
                    .label(monthNames[m - 1])
                    .colOffset(Math.max(0, colOffset))
                    .build());
        }

        return ContributionGraphVO.builder()
                .year(year)
                .total(totalCount)
                .maxDaily(maxDaily)
                .cells(cells)
                .months(months)
                .build();
    }

    private CategoryDistributionVO loadCategoryDistribution(Long userId) {
        List<UserTagAffinity> rows = userTagAffinityMapper.selectList(
                new LambdaQueryWrapper<UserTagAffinity>()
                        .eq(UserTagAffinity::getUserId, userId)
                        .eq(UserTagAffinity::getDimType, DIM_CATEGORY)
                        .orderByDesc(UserTagAffinity::getWeight));

        if (CollectionUtils.isEmpty(rows)) {
            return loadSubmittedCategoryDistribution(userId);
        }

        BigDecimal totalWeight = rows.stream()
                .map(r -> r.getWeight() != null ? r.getWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 名称补全：dimName 为空则查 t_category
        Set<Long> needNameIds = rows.stream()
                .filter(r -> !StringUtils.hasText(r.getDimName()) && r.getDimId() != null)
                .map(UserTagAffinity::getDimId)
                .collect(Collectors.toSet());
        Map<Long, String> idToName = new HashMap<>();
        if (!needNameIds.isEmpty()) {
            List<WebsiteCategory> categories = categoryMapper.selectList(
                    new LambdaQueryWrapper<WebsiteCategory>()
                            .in(WebsiteCategory::getId, needNameIds));
            for (WebsiteCategory c : categories) {
                idToName.put(c.getId(), c.getName());
            }
        }

        List<CategoryShareVO> all = new ArrayList<>();
        for (UserTagAffinity r : rows) {
            BigDecimal w = r.getWeight() != null ? r.getWeight() : BigDecimal.ZERO;
            String name = StringUtils.hasText(r.getDimName())
                    ? r.getDimName()
                    : idToName.getOrDefault(r.getDimId(), "未分类");
            all.add(CategoryShareVO.builder()
                    .categoryId(r.getDimId())
                    .name(name)
                    .weight(w)
                    .pct(percentage(w, totalWeight))
                    .build());
        }

        // TOP-N + 其他聚合
        List<CategoryShareVO> items;
        if (all.size() <= CATEGORY_TOP_N) {
            items = all;
        } else {
            items = new ArrayList<>(all.subList(0, CATEGORY_TOP_N));
            BigDecimal otherWeight = BigDecimal.ZERO;
            for (int i = CATEGORY_TOP_N; i < all.size(); i++) {
                otherWeight = otherWeight.add(all.get(i).getWeight());
            }
            items.add(CategoryShareVO.builder()
                    .categoryId(null)
                    .name("其他")
                    .weight(otherWeight)
                    .pct(percentage(otherWeight, totalWeight))
                    .build());
        }

        return CategoryDistributionVO.builder()
                .totalWeight(totalWeight)
                .items(items)
                .build();
    }

    private CategoryDistributionVO loadSubmittedCategoryDistribution(Long userId) {
        List<Website> websites = websiteMapper.selectList(
                new LambdaQueryWrapper<Website>()
                        .eq(Website::getSubmitterId, userId)
                        .eq(Website::getDeleted, NOT_DELETED)
                        .eq(Website::getAuditStatus, AUDIT_APPROVED_STATUS)
                        .isNotNull(Website::getCategoryId));

        if (CollectionUtils.isEmpty(websites)) {
            return CategoryDistributionVO.builder()
                    .totalWeight(BigDecimal.ZERO)
                    .items(Collections.emptyList())
                    .build();
        }

        Map<Long, Long> categoryCount = websites.stream()
                .filter(w -> w.getCategoryId() != null)
                .collect(Collectors.groupingBy(Website::getCategoryId, Collectors.counting()));

        Set<Long> categoryIds = categoryCount.keySet();
        Map<Long, String> idToName = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<WebsiteCategory> categories = categoryMapper.selectList(
                    new LambdaQueryWrapper<WebsiteCategory>()
                            .in(WebsiteCategory::getId, categoryIds));
            for (WebsiteCategory c : categories) {
                idToName.put(c.getId(), c.getName());
            }
        }

        BigDecimal submitWeight = UserActivityType.SUBMIT.getWeight();
        BigDecimal totalWeight = categoryCount.values().stream()
                .map(count -> submitWeight.multiply(BigDecimal.valueOf(count)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryShareVO> all = categoryCount.entrySet().stream()
                .map(entry -> {
                    BigDecimal weight = submitWeight.multiply(BigDecimal.valueOf(entry.getValue()));
                    return CategoryShareVO.builder()
                            .categoryId(entry.getKey())
                            .name(idToName.getOrDefault(entry.getKey(), "未分类"))
                            .weight(weight)
                            .pct(percentage(weight, totalWeight))
                            .build();
                })
                .sorted((a, b) -> b.getWeight().compareTo(a.getWeight()))
                .collect(Collectors.toList());

        List<CategoryShareVO> items;
        if (all.size() <= CATEGORY_TOP_N) {
            items = all;
        } else {
            items = new ArrayList<>(all.subList(0, CATEGORY_TOP_N));
            BigDecimal otherWeight = BigDecimal.ZERO;
            for (int i = CATEGORY_TOP_N; i < all.size(); i++) {
                otherWeight = otherWeight.add(all.get(i).getWeight());
            }
            items.add(CategoryShareVO.builder()
                    .categoryId(null)
                    .name("其他")
                    .weight(otherWeight)
                    .pct(percentage(otherWeight, totalWeight))
                    .build());
        }

        return CategoryDistributionVO.builder()
                .totalWeight(totalWeight)
                .items(items)
                .build();
    }

    private TechRadarVO loadTechRadar(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("limit 1"));

        // 解析用户技术栈 ID 串
        List<Long> techIds = parseLongCsv(profile != null ? profile.getTechStack() : null);
        Map<Long, String> techNameById = new HashMap<>();
        if (!techIds.isEmpty()) {
            List<TechStack> stacks = techStackMapper.selectList(
                    new LambdaQueryWrapper<TechStack>()
                            .in(TechStack::getId, techIds));
            for (TechStack t : stacks) {
                techNameById.put(t.getId(), t.getName());
            }
        }

        // 6 维分桶：每个技术名按映射归到对应维度，得分 = 该维度命中数
        Map<String, Integer> dimHits = new LinkedHashMap<>();
        for (String d : RADAR_DIMS) {
            dimHits.put(d, 0);
        }
        for (Long id : techIds) {
            String name = techNameById.get(id);
            if (!StringUtils.hasText(name)) {
                continue;
            }
            String dim = matchDim(name);
            if (dim != null) {
                dimHits.merge(dim, 1, Integer::sum);
            }
        }
        int maxHit = dimHits.values().stream().max(Integer::compareTo).orElse(0);
        List<TechRadarDimVO> dimensions = new ArrayList<>(6);
        for (String d : RADAR_DIMS) {
            int hit = dimHits.getOrDefault(d, 0);
            int score = (maxHit == 0) ? 0 : (int) Math.round((hit * 100.0) / maxHit);
            // 至少给一个基线分以避免视觉空白
            score = Math.max(score, hit > 0 ? 35 : 0);
            score = Math.min(100, score);
            dimensions.add(TechRadarDimVO.builder().name(d).value(score).build());
        }

        // Top Languages：按用户填写的技术栈顺序输出占比（等权 + 顺序衰减）
        List<TechLanguageVO> languages = new ArrayList<>();
        if (!techIds.isEmpty()) {
            int n = techIds.size();
            // 顺序衰减权重：第 i 个 = max(1, n - i)
            double totalW = 0d;
            int[] weights = new int[n];
            for (int i = 0; i < n; i++) {
                weights[i] = Math.max(1, n - i);
                totalW += weights[i];
            }
            for (int i = 0; i < n; i++) {
                Long id = techIds.get(i);
                String name = techNameById.getOrDefault(id, "未知");
                BigDecimal pct = BigDecimal.valueOf((weights[i] * 100.0) / totalW)
                        .setScale(1, RoundingMode.HALF_UP);
                languages.add(TechLanguageVO.builder()
                        .techId(id).name(name).pct(pct).build());
            }
        }

        return TechRadarVO.builder()
                .dimensions(dimensions)
                .languages(languages)
                .build();
    }

    private TrendSeriesVO loadTrendSeries(Long userId, int days, String range,
                                          String metric, String column) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);

        List<UserStatsDaily> rows = userStatsDailyMapper.selectList(
                new LambdaQueryWrapper<UserStatsDaily>()
                        .eq(UserStatsDaily::getUserId, userId)
                        .between(UserStatsDaily::getStatDate, startDate, today)
                        .orderByAsc(UserStatsDaily::getStatDate));

        Map<LocalDate, BigDecimal> dayValue = new HashMap<>();
        for (UserStatsDaily row : rows) {
            BigDecimal v = extractMetric(row, column);
            dayValue.put(row.getStatDate(), v);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<TrendPointVO> points = new ArrayList<>(days);
        LocalDate cursor = startDate;
        while (!cursor.isAfter(today)) {
            BigDecimal v = dayValue.getOrDefault(cursor, BigDecimal.ZERO);
            points.add(TrendPointVO.builder().date(cursor.format(fmt)).value(v).build());
            cursor = cursor.plusDays(1);
        }

        BigDecimal delta = computeDelta(points);

        return TrendSeriesVO.builder()
                .range(range)
                .metric(metric)
                .points(points)
                .delta(delta)
                .build();
    }

    // -------------------- 工具方法 --------------------

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或用户标识无效");
        }
    }

    private void validateGraphYear(int year) {
        int now = LocalDate.now().getYear();
        if (year < GRAPH_MIN_YEAR || year > now) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "year 仅支持 [" + GRAPH_MIN_YEAR + ", " + now + "]");
        }
    }

    private int parseRangeDays(String range) {
        if (!StringUtils.hasText(range)) {
            return 30;
        }
        switch (range) {
            case "7d": return 7;
            case "30d": return 30;
            case "90d": return 90;
            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "range 仅支持 7d / 30d / 90d");
        }
    }

    private String parseMetricColumn(String metric) {
        if (!StringUtils.hasText(metric)) {
            return "browse_count";
        }
        switch (metric) {
            case "pv": return "browse_count";
            case "likes": return "like_count";
            case "favorites": return "collect_count";
            case "comments": return "comment_count";
            case "contribution": return "contribution";
            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "metric 仅支持 pv / likes / favorites / comments / contribution");
        }
    }

    private BigDecimal extractMetric(UserStatsDaily row, String column) {
        return switch (column) {
            case "browse_count" -> BigDecimal.valueOf(nz(row.getBrowseCount()));
            case "like_count" -> BigDecimal.valueOf(nz(row.getLikeCount()));
            case "collect_count" -> BigDecimal.valueOf(nz(row.getCollectCount()));
            case "comment_count" -> BigDecimal.valueOf(nz(row.getCommentCount()));
            case "contribution" -> row.getContribution() != null ? row.getContribution() : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 前后半段平均值环比变化。points 必须按时间升序。
     */
    private BigDecimal computeDelta(List<TrendPointVO> points) {
        if (points == null || points.size() < 2) {
            return BigDecimal.ZERO;
        }
        int half = points.size() / 2;
        BigDecimal prevSum = BigDecimal.ZERO;
        BigDecimal currSum = BigDecimal.ZERO;
        for (int i = 0; i < points.size(); i++) {
            BigDecimal v = points.get(i).getValue();
            if (v == null) v = BigDecimal.ZERO;
            if (i < half) prevSum = prevSum.add(v);
            else currSum = currSum.add(v);
        }
        if (prevSum.signum() == 0) {
            return currSum.signum() == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(100.0).setScale(1, RoundingMode.HALF_UP);
        }
        return currSum.subtract(prevSum)
                .multiply(BigDecimal.valueOf(100))
                .divide(prevSum, 1, RoundingMode.HALF_UP);
    }

    private int computeCellLevel(int count, int maxDaily) {
        if (count <= 0) return 0;
        if (maxDaily <= 0) return 0;
        double ratio = (double) count / maxDaily;
        if (ratio <= 0.25) return 1;
        if (ratio <= 0.5) return 2;
        if (ratio <= 0.75) return 3;
        return 4;
    }

    private int computeCurrentStreak(Long userId, LocalDate lastActiveDate) {
        if (lastActiveDate == null) {
            return 0;
        }
        // 仅当 lastActiveDate 是今天或昨天才认为 streak 有效
        LocalDate today = LocalDate.now();
        if (lastActiveDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        // 回溯最多 365 天
        LocalDate from = lastActiveDate.minusDays(365);
        List<UserStatsDaily> rows = userStatsDailyMapper.selectList(
                new LambdaQueryWrapper<UserStatsDaily>()
                        .eq(UserStatsDaily::getUserId, userId)
                        .between(UserStatsDaily::getStatDate, from, lastActiveDate)
                        .orderByDesc(UserStatsDaily::getStatDate));
        int streak = 0;
        LocalDate expect = lastActiveDate;
        for (UserStatsDaily row : rows) {
            if (!Objects.equals(row.getStatDate(), expect)) {
                break;
            }
            int sum = nz(row.getSubmitCount()) + nz(row.getCommentCount())
                    + nz(row.getCollectCount()) + nz(row.getLikeCount())
                    + nz(row.getScoreCount()) + nz(row.getBrowseCount());
            if (sum <= 0) {
                break;
            }
            streak++;
            expect = expect.minusDays(1);
        }
        return streak;
    }

    private String computeLevelCode(BigDecimal totalContribution) {
        if (totalContribution == null) return "C";
        double v = totalContribution.doubleValue();
        if (v >= 5000) return "S";
        if (v >= 2000) return "A";
        if (v >= 500) return "B";
        return "C";
    }

    private String matchDim(String techName) {
        if (!StringUtils.hasText(techName)) return null;
        String low = techName.toLowerCase(Locale.ROOT).trim();
        // 完全匹配优先
        String dim = TECH_TO_DIM.get(low);
        if (dim != null) return dim;
        // 关键词 contains 兜底
        for (Map.Entry<String, String> e : TECH_TO_DIM.entrySet()) {
            if (low.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    private List<Long> parseLongCsv(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (String s : csv.split(",")) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            try {
                result.add(Long.parseLong(t));
            } catch (NumberFormatException ignored) {
                // 忽略非法 ID，前端展示时不可见
            }
        }
        return result;
    }

    private long epochWeek(LocalDate date, WeekFields weekFields) {
        // ISO 周年（含跨年），用周年的"周序"做近似线性化
        return date.toEpochDay() / 7;
    }

    private BigDecimal percentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.signum() == 0 || part == null) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
