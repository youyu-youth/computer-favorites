package com.yyyouth.service.user.userstats.impl;

import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO.DailyPointVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO.DeltaGroupVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO.MetricGroupVO;
import com.yyyouth.service.mapper.user.UploadImpactMapper;
import com.yyyouth.service.mapper.user.dto.DateCountRow;
import com.yyyouth.service.mapper.user.dto.WebsiteCounterRow;
import com.yyyouth.service.config.redis.RedisCache;
import com.yyyouth.service.user.userstats.UploadImpactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力服务实现（user-15 扩展）。
 *
 * 设计要点：
 *  1. 累计列（totals）从 t_website 实时计数器 SUM，零事件表扫描；
 *  2. 时间序列查询合并 current + prev 区间为一次 [prevFrom, currTo) 查询，5 表 5 次 SQL（而非 10 次）；
 *  3. 5 表查询用 {@link CompletableFuture} 并发执行，墙钟时间 ≈ 最慢单表；
 *  4. Cache-Aside（{@link RedisCache#getOrLoad}）TTL 600s + jitter 60s，纯 TTL 自然失效；
 *  5. range=all 跳过事件表查询；websiteCount=0 直接返回零值 VO。
 *
 * 性能说明：本期不加索引（spec P2），大数据量下 t_browse_history 是主要瓶颈，
 * 命中缓存时 P95 < 50ms；缓存 miss 时 P95 取决于慢日志告警与索引补齐节奏。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadImpactServiceImpl implements UploadImpactService {

    /** Cache TTL（秒） */
    private static final long TTL_UPLOAD_IMPACT_SECONDS = 600L;

    /** Cache TTL 抖动（秒） */
    private static final long JITTER_60_SECONDS = 60L;

    /** range：全部 */
    private static final String RANGE_ALL = "all";

    /** range：7 天 */
    private static final String RANGE_7D = "7d";

    /** range：30 天 */
    private static final String RANGE_30D = "30d";

    /** range：90 天 */
    private static final String RANGE_90D = "90d";

    /** ISO 日期格式 */
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UploadImpactMapper uploadImpactMapper;

    private final RedisCache redisCache;

    @Override
    public UploadImpactVO getUploadImpact(Long userId, String range) {
        validateUserId(userId);
        String normalizedRange = validateRange(range);

        String key = RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId
                + ":upload-impact:" + normalizedRange;
        return redisCache.getOrLoad(key, UploadImpactVO.class,
                TTL_UPLOAD_IMPACT_SECONDS, JITTER_60_SECONDS,
                () -> loadUploadImpact(userId, normalizedRange));
    }

    // -------------------- 回源实现 --------------------

    private UploadImpactVO loadUploadImpact(Long userId, String range) {
        // 1. 拿用户上传网站列表 + 累计计数器
        List<WebsiteCounterRow> sites = uploadImpactMapper.selectSitesBySubmitter(userId);
        int websiteCount = sites == null ? 0 : sites.size();
        MetricGroupVO totals = sumTotals(sites);

        // 2. range=all 或 无网站 → 直接返回，不查事件表
        if (RANGE_ALL.equals(range) || websiteCount == 0) {
            return UploadImpactVO.builder()
                    .range(range)
                    .websiteCount(websiteCount)
                    .totals(totals)
                    .rangeCounts(RANGE_ALL.equals(range) ? totals : zeroMetric())
                    .delta(RANGE_ALL.equals(range) ? nullDelta() : zeroDelta())
                    .points(Collections.emptyList())
                    .build();
        }

        // 3. 计算时间窗口
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDate currFrom = today.minusDays(days - 1L);
        LocalDate currToExclusive = today.plusDays(1L);
        LocalDate prevFrom = currFrom.minusDays(days);

        LocalDateTime queryFrom = prevFrom.atStartOfDay();
        LocalDateTime queryTo = currToExclusive.atStartOfDay();

        List<Long> websiteIds = sites.stream()
                .map(WebsiteCounterRow::getId)
                .collect(Collectors.toList());

        // 4. 5 表并发查询 [prevFrom, currTo) 整段
        CompletableFuture<List<DateCountRow>> fBrowse = CompletableFuture.supplyAsync(
                () -> uploadImpactMapper.countBrowse(websiteIds, queryFrom, queryTo));
        CompletableFuture<List<DateCountRow>> fLike = CompletableFuture.supplyAsync(
                () -> uploadImpactMapper.countLike(websiteIds, queryFrom, queryTo));
        CompletableFuture<List<DateCountRow>> fCollect = CompletableFuture.supplyAsync(
                () -> uploadImpactMapper.countCollect(websiteIds, queryFrom, queryTo));
        CompletableFuture<List<DateCountRow>> fComment = CompletableFuture.supplyAsync(
                () -> uploadImpactMapper.countComment(websiteIds, queryFrom, queryTo));
        CompletableFuture<List<DateCountRow>> fScore = CompletableFuture.supplyAsync(
                () -> uploadImpactMapper.countScore(websiteIds, queryFrom, queryTo));
        CompletableFuture.allOf(fBrowse, fLike, fCollect, fComment, fScore).join();

        Map<LocalDate, Long> browseMap = toMap(fBrowse.join());
        Map<LocalDate, Long> likeMap = toMap(fLike.join());
        Map<LocalDate, Long> collectMap = toMap(fCollect.join());
        Map<LocalDate, Long> commentMap = toMap(fComment.join());
        Map<LocalDate, Long> scoreMap = toMap(fScore.join());

        // 5. 拆 prev / curr，组装 points
        long prevBrowse = sumIfBefore(browseMap, currFrom);
        long prevLike = sumIfBefore(likeMap, currFrom);
        long prevCollect = sumIfBefore(collectMap, currFrom);
        long prevComment = sumIfBefore(commentMap, currFrom);
        long prevScore = sumIfBefore(scoreMap, currFrom);

        List<DailyPointVO> points = new ArrayList<>(days);
        long currBrowse = 0L, currLike = 0L, currCollect = 0L, currComment = 0L, currScore = 0L;
        LocalDate cursor = currFrom;
        while (!cursor.isAfter(today)) {
            long b = browseMap.getOrDefault(cursor, 0L);
            long l = likeMap.getOrDefault(cursor, 0L);
            long c = collectMap.getOrDefault(cursor, 0L);
            long cm = commentMap.getOrDefault(cursor, 0L);
            long s = scoreMap.getOrDefault(cursor, 0L);

            currBrowse += b;
            currLike += l;
            currCollect += c;
            currComment += cm;
            currScore += s;

            points.add(DailyPointVO.builder()
                    .date(cursor.format(ISO_DATE))
                    .browse(b)
                    .like(l)
                    .collect(c)
                    .comment(cm)
                    .score(s)
                    .build());
            cursor = cursor.plusDays(1L);
        }

        // 6. 组装 rangeCounts / delta
        MetricGroupVO rangeCounts = MetricGroupVO.builder()
                .browse(currBrowse).like(currLike).collect(currCollect)
                .comment(currComment).score(currScore)
                .build();

        DeltaGroupVO delta = DeltaGroupVO.builder()
                .browse(computeDelta(prevBrowse, currBrowse))
                .like(computeDelta(prevLike, currLike))
                .collect(computeDelta(prevCollect, currCollect))
                .comment(computeDelta(prevComment, currComment))
                .score(computeDelta(prevScore, currScore))
                .build();

        return UploadImpactVO.builder()
                .range(range)
                .websiteCount(websiteCount)
                .totals(totals)
                .rangeCounts(rangeCounts)
                .delta(delta)
                .points(points)
                .build();
    }

    // -------------------- 工具方法 --------------------

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或用户标识无效");
        }
    }

    private String validateRange(String range) {
        if (!StringUtils.hasText(range)) {
            return RANGE_30D;
        }
        return switch (range) {
            case RANGE_7D, RANGE_30D, RANGE_90D, RANGE_ALL -> range;
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "range 仅支持 7d / 30d / 90d / all");
        };
    }

    private int parseRangeDays(String range) {
        return switch (range) {
            case RANGE_7D -> 7;
            case RANGE_30D -> 30;
            case RANGE_90D -> 90;
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "range 仅支持 7d / 30d / 90d");
        };
    }

    private MetricGroupVO sumTotals(List<WebsiteCounterRow> sites) {
        if (CollectionUtils.isEmpty(sites)) {
            return zeroMetric();
        }
        long browse = 0L, like = 0L, collect = 0L, comment = 0L, score = 0L;
        for (WebsiteCounterRow row : sites) {
            browse += nz(row.getClickCount());
            like += nz(row.getLikeCount());
            collect += nz(row.getCollectCount());
            comment += nz(row.getCommentCount());
            score += nz(row.getScoreCount());
        }
        return MetricGroupVO.builder()
                .browse(browse).like(like).collect(collect)
                .comment(comment).score(score)
                .build();
    }

    private MetricGroupVO zeroMetric() {
        return MetricGroupVO.builder()
                .browse(0L).like(0L).collect(0L).comment(0L).score(0L)
                .build();
    }

    private DeltaGroupVO zeroDelta() {
        BigDecimal zero = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        return DeltaGroupVO.builder()
                .browse(zero).like(zero).collect(zero).comment(zero).score(zero)
                .build();
    }

    private DeltaGroupVO nullDelta() {
        return DeltaGroupVO.builder().build();
    }

    private Map<LocalDate, Long> toMap(List<DateCountRow> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        return rows.stream().collect(Collectors.toMap(
                DateCountRow::getD,
                r -> r.getC() == null ? 0L : r.getC(),
                Long::sum));
    }

    private long sumIfBefore(Map<LocalDate, Long> map, LocalDate boundary) {
        if (map == null || map.isEmpty()) {
            return 0L;
        }
        long sum = 0L;
        for (Map.Entry<LocalDate, Long> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().isBefore(boundary)) {
                sum += entry.getValue() == null ? 0L : entry.getValue();
            }
        }
        return sum;
    }

    /**
     * 环比百分比（HALF_UP 1 位小数）。
     * prev=0 && curr=0 → 0；prev=0 && curr>0 → 100；其他 → (curr - prev) * 100 / prev
     */
    private BigDecimal computeDelta(long prev, long curr) {
        if (prev == 0L && curr == 0L) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        if (prev == 0L) {
            return BigDecimal.valueOf(100L).setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(curr - prev)
                .multiply(BigDecimal.valueOf(100L))
                .divide(BigDecimal.valueOf(prev), 1, RoundingMode.HALF_UP);
    }

    private long nz(Integer v) {
        return v == null ? 0L : v.longValue();
    }
}
