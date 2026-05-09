package com.yyyouth.service.user.userstats;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.yyyouth.model.pojo.user.UserStatsDaily;
import com.yyyouth.model.pojo.user.UserStatsOverview;
import com.yyyouth.service.mapper.user.UserStatsAggregateMapper;
import com.yyyouth.service.mapper.user.UserStatsDailyMapper;
import com.yyyouth.service.mapper.user.UserStatsOverviewMapper;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.impl.UserStatsRecomputeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * T+1 重算服务单元测试（user-15 M4）。
 *
 * 覆盖：
 *  - recomputeDaily：按权重 + 单日 50 上限计算 contribution，先 DELETE 后 upsertIncr 等价覆写；
 *  - recomputeOverview：level 分级；streak 计算；rank_percent 同分共享。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserStatsRecomputeServiceImplTest {

    @Mock
    private UserStatsAggregateMapper aggregateMapper;

    @Mock
    private UserStatsDailyMapper dailyMapper;

    @Mock
    private UserStatsOverviewMapper overviewMapper;

    @Mock
    private RedisCache redisCache;

    private UserStatsRecomputeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserStatsRecomputeServiceImpl(aggregateMapper, dailyMapper, overviewMapper, redisCache);
    }

    // ========================= recomputeDaily =========================

    @Test
    void recomputeDaily_appliesWeightAndCap() {
        LocalDate date = LocalDate.of(2026, 5, 7);
        when(aggregateMapper.findActiveUserIds(date)).thenReturn(List.of(1001L));

        // 计数：submit=2, comment=3, collect=4, like=10, score=2, browse=20
        // contribution = 2*5 + 3*2 + 4*1 + 10*0.5 + 2*1 + 20*0.1 = 10+6+4+5+2+2 = 29.0
        Map<String, Object> counts = new HashMap<>();
        counts.put("submit_count", 2);
        counts.put("comment_count", 3);
        counts.put("collect_count", 4);
        counts.put("like_count", 10);
        counts.put("score_count", 2);
        counts.put("browse_count", 20);
        when(aggregateMapper.aggregateDailyCounts(1001L, date)).thenReturn(counts);

        int affected = service.recomputeDaily(date);

        assertThat(affected).isEqualTo(1);
        ArgumentCaptor<BigDecimal> capContrib = ArgumentCaptor.forClass(BigDecimal.class);
        verify(dailyMapper).upsertIncr(eq(1001L), eq(date),
                eq(2), eq(3), eq(4), eq(10), eq(2), eq(20),
                capContrib.capture());
        assertThat(capContrib.getValue()).isEqualByComparingTo(new BigDecimal("29.0"));
    }

    @Test
    void recomputeDaily_capsContributionAt50() {
        LocalDate date = LocalDate.of(2026, 5, 7);
        when(aggregateMapper.findActiveUserIds(date)).thenReturn(List.of(2001L));

        // 投稿 20 次 -> 100 分，触发上限 50
        Map<String, Object> counts = new HashMap<>();
        counts.put("submit_count", 20);
        counts.put("comment_count", 0);
        counts.put("collect_count", 0);
        counts.put("like_count", 0);
        counts.put("score_count", 0);
        counts.put("browse_count", 0);
        when(aggregateMapper.aggregateDailyCounts(2001L, date)).thenReturn(counts);

        service.recomputeDaily(date);

        ArgumentCaptor<BigDecimal> capContrib = ArgumentCaptor.forClass(BigDecimal.class);
        verify(dailyMapper).upsertIncr(eq(2001L), eq(date),
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                capContrib.capture());
        assertThat(capContrib.getValue()).isEqualByComparingTo(new BigDecimal("50.0"));
    }

    @Test
    void recomputeDaily_deletesBeforeInsertToAvoidDoubleCount() {
        LocalDate date = LocalDate.of(2026, 5, 7);
        when(aggregateMapper.findActiveUserIds(date)).thenReturn(List.of(3001L));
        when(aggregateMapper.aggregateDailyCounts(eq(3001L), eq(date)))
                .thenReturn(Map.of(
                        "submit_count", 1, "comment_count", 0, "collect_count", 0,
                        "like_count", 0, "score_count", 0, "browse_count", 0));

        service.recomputeDaily(date);

        // 必须先 delete 再 upsertIncr
        verify(dailyMapper).delete(any(Wrapper.class));
        verify(dailyMapper).upsertIncr(eq(3001L), eq(date),
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class));
    }

    // ========================= recomputeOverview =========================

    @Test
    void recomputeOverview_levelAndRank() {
        // 3 用户：4001=6000(S, rank 0), 4002=2500(A+, rank 1), 4003=100(C, rank 2)
        when(aggregateMapper.findAllOverviewUserIds()).thenReturn(List.of(4001L, 4002L, 4003L));
        List<Map<String, Object>> snapshot = new ArrayList<>();
        snapshot.add(Map.of("user_id", 4001L, "total_contribution", new BigDecimal("6000")));
        snapshot.add(Map.of("user_id", 4002L, "total_contribution", new BigDecimal("2500")));
        snapshot.add(Map.of("user_id", 4003L, "total_contribution", new BigDecimal("100")));
        when(aggregateMapper.listAllContributionDesc()).thenReturn(snapshot);

        when(aggregateMapper.aggregateOverviewFromDaily(4001L)).thenReturn(buildOverviewAgg(6000));
        when(aggregateMapper.aggregateOverviewFromDaily(4002L)).thenReturn(buildOverviewAgg(2500));
        when(aggregateMapper.aggregateOverviewFromDaily(4003L)).thenReturn(buildOverviewAgg(100));
        when(aggregateMapper.listActiveDaysDesc(anyLong(), any())).thenReturn(List.of());
        when(aggregateMapper.listAllActiveDaysAsc(anyLong())).thenReturn(List.of());
        when(overviewMapper.updateById(any(UserStatsOverview.class))).thenReturn(1);

        service.recomputeOverview();

        ArgumentCaptor<UserStatsOverview> cap = ArgumentCaptor.forClass(UserStatsOverview.class);
        verify(overviewMapper, org.mockito.Mockito.times(3)).updateById(cap.capture());
        Map<Long, UserStatsOverview> map = new HashMap<>();
        for (UserStatsOverview po : cap.getAllValues()) map.put(po.getUserId(), po);

        assertThat(map.get(4001L).getLevelCode()).isEqualTo("S");
        assertThat(map.get(4002L).getLevelCode()).isEqualTo("A+");
        assertThat(map.get(4003L).getLevelCode()).isEqualTo("C");

        // rank_percent 语义：打败的比例
        // 4001 (rank 0): higher=0, defeated = 3-0-1 = 2, percent = 2/3*100 = 66.67
        assertThat(map.get(4001L).getRankPercent()).isEqualByComparingTo(new BigDecimal("66.67"));
        // 4002 (rank 1): higher=1, defeated = 1, percent = 33.33
        assertThat(map.get(4002L).getRankPercent()).isEqualByComparingTo(new BigDecimal("33.33"));
        // 4003 (rank 2): higher=2, defeated = 0, percent = 0
        assertThat(map.get(4003L).getRankPercent()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void recomputeOverview_streakComputeFromActiveDays() {
        Long uid = 5001L;
        when(aggregateMapper.findAllOverviewUserIds()).thenReturn(List.of(uid));
        when(aggregateMapper.listAllContributionDesc())
                .thenReturn(List.of(Map.of("user_id", uid, "total_contribution", new BigDecimal("500"))));
        when(aggregateMapper.aggregateOverviewFromDaily(uid)).thenReturn(buildOverviewAgg(500));

        // lastActive = 昨天，streak 应 >= 1；倒序近 3 天连续
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        // 模拟 daily 聚合返回 lastActive=yesterday
        Map<String, Object> agg = buildOverviewAgg(500);
        agg.put("last_active_date", java.sql.Date.valueOf(yesterday));
        when(aggregateMapper.aggregateOverviewFromDaily(uid)).thenReturn(agg);

        when(aggregateMapper.listActiveDaysDesc(eq(uid), any())).thenReturn(List.of(
                Map.of("stat_date", java.sql.Date.valueOf(yesterday)),
                Map.of("stat_date", java.sql.Date.valueOf(twoDaysAgo)),
                Map.of("stat_date", java.sql.Date.valueOf(threeDaysAgo))
        ));
        when(aggregateMapper.listAllActiveDaysAsc(eq(uid))).thenReturn(List.of(
                threeDaysAgo, twoDaysAgo, yesterday));
        when(overviewMapper.updateById(any(UserStatsOverview.class))).thenReturn(1);

        service.recomputeOverview();

        ArgumentCaptor<UserStatsOverview> cap = ArgumentCaptor.forClass(UserStatsOverview.class);
        verify(overviewMapper).updateById(cap.capture());
        UserStatsOverview po = cap.getValue();
        assertThat(po.getStreakDays()).isEqualTo(3);
        assertThat(po.getMaxStreakDays()).isEqualTo(3);
        assertThat(po.getLastActiveDate()).isEqualTo(yesterday);
    }

    @Test
    void recomputeOverview_inactiveTooLongResetsStreak() {
        Long uid = 6001L;
        when(aggregateMapper.findAllOverviewUserIds()).thenReturn(List.of(uid));
        when(aggregateMapper.listAllContributionDesc())
                .thenReturn(List.of(Map.of("user_id", uid, "total_contribution", BigDecimal.ZERO)));

        // lastActive = 5 天前，超出"昨天/今天"阈值 -> streak=0
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
        Map<String, Object> agg = buildOverviewAgg(0);
        agg.put("last_active_date", java.sql.Date.valueOf(fiveDaysAgo));
        when(aggregateMapper.aggregateOverviewFromDaily(uid)).thenReturn(agg);
        when(aggregateMapper.listActiveDaysDesc(eq(uid), any())).thenReturn(List.of());
        when(aggregateMapper.listAllActiveDaysAsc(eq(uid))).thenReturn(List.of(fiveDaysAgo));
        when(overviewMapper.updateById(any(UserStatsOverview.class))).thenReturn(1);

        service.recomputeOverview();

        ArgumentCaptor<UserStatsOverview> cap = ArgumentCaptor.forClass(UserStatsOverview.class);
        verify(overviewMapper).updateById(cap.capture());
        assertThat(cap.getValue().getStreakDays()).isEqualTo(0);
        assertThat(cap.getValue().getMaxStreakDays()).isEqualTo(1);
    }

    private Map<String, Object> buildOverviewAgg(int contribution) {
        Map<String, Object> map = new HashMap<>();
        map.put("total_submit", 0);
        map.put("total_comment", 0);
        map.put("total_collect", 0);
        map.put("total_like", 0);
        map.put("total_score", 0);
        map.put("total_browse", 0);
        map.put("total_contribution", new BigDecimal(contribution));
        map.put("last_active_date", null);
        return map;
    }

    private static long anyLong() {
        return org.mockito.ArgumentMatchers.anyLong();
    }
}
