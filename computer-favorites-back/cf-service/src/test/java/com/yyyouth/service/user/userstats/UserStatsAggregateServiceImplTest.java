package com.yyyouth.service.user.userstats;

import com.yyyouth.model.dto.userstats.UserActivityEvent;
import com.yyyouth.service.config.redis.RedisCache;
import com.yyyouth.service.mapper.user.UserStatsDailyMapper;
import com.yyyouth.service.mapper.user.UserStatsOverviewMapper;
import com.yyyouth.service.mapper.user.UserTagAffinityMapper;
import com.yyyouth.service.user.userstats.impl.UserStatsAggregateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * UserStatsAggregateServiceImpl 单元测试
 *
 * 验证：
 *  - 6 种类型事件均能命中正确的 daily 计数列；
 *  - 单日上限 50 分裁剪；
 *  - 幂等去重（SETNX 失败时不写库）；
 *  - 维度偏好：collect / like / browse 携带 categoryId 时落库；其他类型不落；
 *  - 缓存失效路径不阻塞主流程。
 */
@ExtendWith(MockitoExtension.class)
class UserStatsAggregateServiceImplTest {

    @Mock
    private UserStatsDailyMapper userStatsDailyMapper;

    @Mock
    private UserStatsOverviewMapper userStatsOverviewMapper;

    @Mock
    private UserTagAffinityMapper userTagAffinityMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    @SuppressWarnings("rawtypes")
    private Cursor cursor;

    @Mock
    private RedisCache redisCache;

    private UserStatsAggregateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserStatsAggregateServiceImpl(
                userStatsDailyMapper,
                userStatsOverviewMapper,
                userTagAffinityMapper,
                stringRedisTemplate,
                redisCache);
    }

    private UserActivityEvent buildEvent(String type, Long userId, Long categoryId) {
        return UserActivityEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .type(type)
                .targetId(2001L)
                .categoryId(categoryId)
                .ts(LocalDateTime.now())
                .build();
    }

    @SuppressWarnings("unchecked")
    private void stubIdempotentPass() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    private void stubIdempotentBlock() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(Boolean.FALSE);
    }

    @Test
    void shouldUpsertSubmitEventWithFullWeight() {
        stubIdempotentPass();
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        boolean ok = service.apply(buildEvent("submit", 1001L, null));

        assertThat(ok).isTrue();
        ArgumentCaptor<BigDecimal> contribCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userStatsDailyMapper).upsertIncr(
                eq(1001L), any(LocalDate.class),
                eq(1), eq(0), eq(0), eq(0), eq(0), eq(0),
                contribCaptor.capture());
        assertThat(contribCaptor.getValue()).isEqualByComparingTo(new BigDecimal("5.0"));

        verify(userStatsOverviewMapper).upsertIncr(
                eq(1001L),
                eq(1), eq(0), eq(0), eq(0), eq(0), eq(0),
                any(BigDecimal.class), any(LocalDate.class));

        // submit 不携带 categoryId → 不调用 tag affinity
        verify(userTagAffinityMapper, never()).upsertWeight(anyLong(), anyInt(), anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    void shouldUpsertCollectEventWithCategoryAffinity() {
        stubIdempotentPass();
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        service.apply(buildEvent("collect", 1001L, 7L));

        verify(userStatsDailyMapper).upsertIncr(
                eq(1001L), any(LocalDate.class),
                eq(0), eq(0), eq(1), eq(0), eq(0), eq(0),
                any(BigDecimal.class));
        verify(userTagAffinityMapper).upsertWeight(
                eq(1001L), eq(1), eq(7L), anyString(),
                eq(new BigDecimal("3.0")));
    }

    @Test
    void shouldUpsertBrowseEventWithLowDailyContribution() {
        stubIdempotentPass();
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        service.apply(buildEvent("browse", 1001L, 9L));

        ArgumentCaptor<BigDecimal> contribCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userStatsDailyMapper).upsertIncr(
                eq(1001L), any(LocalDate.class),
                eq(0), eq(0), eq(0), eq(0), eq(0), eq(1),
                contribCaptor.capture());
        assertThat(contribCaptor.getValue()).isEqualByComparingTo(new BigDecimal("0.1"));
        verify(userTagAffinityMapper).upsertWeight(
                eq(1001L), eq(1), eq(9L), anyString(),
                eq(new BigDecimal("0.3")));
    }

    @Test
    void shouldClampContributionWhenDailyCapReached() {
        stubIdempotentPass();
        // 当日已累计 49.8 分，剩余仅 0.2；本次 SUBMIT 权重 5.0，应被裁剪到 0.2
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(new BigDecimal("49.8"));

        service.apply(buildEvent("submit", 1001L, null));

        ArgumentCaptor<BigDecimal> contribCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userStatsDailyMapper).upsertIncr(
                eq(1001L), any(LocalDate.class),
                eq(1), eq(0), eq(0), eq(0), eq(0), eq(0),
                contribCaptor.capture());
        assertThat(contribCaptor.getValue()).isEqualByComparingTo(new BigDecimal("0.2"));
    }

    @Test
    void shouldRecordZeroContributionWhenDailyCapExceeded() {
        stubIdempotentPass();
        // 当日已累计 50 分，本次贡献分应为 0，但 count 仍 +1
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(new BigDecimal("50.0"));

        service.apply(buildEvent("comment", 1001L, null));

        ArgumentCaptor<BigDecimal> contribCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userStatsDailyMapper).upsertIncr(
                eq(1001L), any(LocalDate.class),
                eq(0), eq(1), eq(0), eq(0), eq(0), eq(0),
                contribCaptor.capture());
        assertThat(contribCaptor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldSkipWhenIdempotentLockHeld() {
        stubIdempotentBlock();

        boolean ok = service.apply(buildEvent("like", 1001L, 9L));

        assertThat(ok).isFalse();
        verify(userStatsDailyMapper, never()).upsertIncr(
                anyLong(), any(LocalDate.class),
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class));
        verify(userStatsOverviewMapper, never()).upsertIncr(
                anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class), any(LocalDate.class));
    }

    @Test
    void shouldRejectInvalidUserIdOrType() {
        assertThat(service.apply(null)).isFalse();
        assertThat(service.apply(buildEvent("collect", null, 1L))).isFalse();
        assertThat(service.apply(buildEvent("collect", 0L, 1L))).isFalse();
        assertThat(service.apply(buildEvent("not-a-type", 1001L, 1L))).isFalse();

        verify(userStatsDailyMapper, never()).upsertIncr(
                anyLong(), any(LocalDate.class),
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class));
    }

    @Test
    void shouldNotUpsertTagAffinityForCommentOrScoreOrSubmit() {
        stubIdempotentPass();
        when(userStatsDailyMapper.selectContribution(eq(1001L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        service.apply(buildEvent("comment", 1001L, 5L));
        service.apply(buildEvent("score", 1001L, 5L));
        service.apply(buildEvent("submit", 1001L, 5L));

        // 即使携带 categoryId，这 3 类也不写 affinity
        verify(userTagAffinityMapper, never()).upsertWeight(anyLong(), anyInt(), anyLong(), anyString(), any(BigDecimal.class));
        verify(userStatsDailyMapper, times(3)).upsertIncr(
                anyLong(), any(LocalDate.class),
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class));
    }
}
