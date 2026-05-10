package com.yyyouth.service.user.userstats;

import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.service.mapper.user.UploadImpactMapper;
import com.yyyouth.service.mapper.user.dto.DateCountRow;
import com.yyyouth.service.mapper.user.dto.WebsiteCounterRow;
import com.yyyouth.service.redis.RedisCache;
import com.yyyouth.service.user.userstats.impl.UploadImpactServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * {@link UploadImpactServiceImpl} 单元测试。
 *
 * 覆盖：
 *  - 用户无审核通过网站 → 全 0；
 *  - range=7d 序列对齐与缺失日补 0；
 *  - delta 边界（prev=0 curr=0 / prev=0 curr&gt;0 / prev&gt;0 curr&lt;prev）；
 *  - range=all 跳过事件表查询；
 *  - 非法 range 抛 400；
 *  - 缓存命中路径：第二次调用不再走 mapper。
 *
 * 注意：mock {@link RedisCache#getOrLoad} 时大多数用例直接调用 supplier，
 * 仅缓存命中用例返回预构建 VO。
 */
@ExtendWith(MockitoExtension.class)
class UploadImpactServiceImplTest {

    @Mock
    private UploadImpactMapper mapper;

    @Mock
    private RedisCache redisCache;

    private UploadImpactServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UploadImpactServiceImpl(mapper, redisCache);
    }

    // -------------------- 用例 --------------------

    @Test
    void getUploadImpact_userWithoutSites_returnsAllZeros() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(1L)).thenReturn(Collections.emptyList());

        UploadImpactVO vo = service.getUploadImpact(1L, "30d");

        assertThat(vo.getRange()).isEqualTo("30d");
        assertThat(vo.getWebsiteCount()).isZero();
        assertMetricAllZero(vo.getTotals());
        assertMetricAllZero(vo.getRangeCounts());
        assertThat(vo.getDelta().getBrowse().signum()).isZero();
        assertThat(vo.getDelta().getLike().signum()).isZero();
        assertThat(vo.getPoints()).isEmpty();

        verify(mapper, never()).countBrowse(anyList(), any(), any());
        verify(mapper, never()).countLike(anyList(), any(), any());
        verify(mapper, never()).countCollect(anyList(), any(), any());
        verify(mapper, never()).countComment(anyList(), any(), any());
        verify(mapper, never()).countScore(anyList(), any(), any());
    }

    @Test
    void getUploadImpact_range7d_buildsAscendingPointsWithZeroFill() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(2L)).thenReturn(List.of(
                site(101L, 100, 10, 5, 3, 2)
        ));
        // 仅在「今天」给一个浏览事件，验证 7 个点中除了今天都是 0
        LocalDate today = LocalDate.now();
        when(mapper.countBrowse(anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new DateCountRow(today, 7L)));
        when(mapper.countLike(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countCollect(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countComment(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countScore(anyList(), any(), any())).thenReturn(Collections.emptyList());

        UploadImpactVO vo = service.getUploadImpact(2L, "7d");

        assertThat(vo.getPoints()).hasSize(7);
        // 升序：第 0 个是 6 天前
        assertThat(vo.getPoints().get(0).getDate())
                .isEqualTo(today.minusDays(6).toString());
        // 最后一个是今天
        assertThat(vo.getPoints().get(6).getDate()).isEqualTo(today.toString());
        // 缺失日补 0
        assertThat(vo.getPoints().get(0).getBrowse()).isZero();
        // 今天有 7 次浏览
        assertThat(vo.getPoints().get(6).getBrowse()).isEqualTo(7L);
        // rangeCounts.browse = 7
        assertThat(vo.getRangeCounts().getBrowse()).isEqualTo(7L);
        assertThat(vo.getRangeCounts().getLike()).isZero();
        // totals 来自 t_website 计数器
        assertThat(vo.getTotals().getBrowse()).isEqualTo(100L);
        assertThat(vo.getTotals().getLike()).isEqualTo(10L);
    }

    @Test
    void getUploadImpact_delta_prevZeroCurrZero_returnsZero() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(3L)).thenReturn(List.of(site(201L, 0, 0, 0, 0, 0)));
        // 全部空 → curr=0, prev=0
        stubAllEmpty();

        UploadImpactVO vo = service.getUploadImpact(3L, "30d");

        assertThat(vo.getDelta().getBrowse().signum()).isZero();
        assertThat(vo.getDelta().getLike().signum()).isZero();
    }

    @Test
    void getUploadImpact_delta_prevZeroCurrPositive_returns100() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(4L)).thenReturn(List.of(site(301L, 0, 0, 0, 0, 0)));
        LocalDate today = LocalDate.now();
        // 今天 5 次点赞、上期段全 0
        when(mapper.countLike(anyList(), any(), any()))
                .thenReturn(List.of(new DateCountRow(today, 5L)));
        when(mapper.countBrowse(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countCollect(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countComment(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countScore(anyList(), any(), any())).thenReturn(Collections.emptyList());

        UploadImpactVO vo = service.getUploadImpact(4L, "30d");

        assertThat(vo.getDelta().getLike().intValue()).isEqualTo(100);
    }

    @Test
    void getUploadImpact_delta_prevPositiveCurrLess_returnsNegative() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(5L)).thenReturn(List.of(site(401L, 0, 0, 0, 0, 0)));
        LocalDate today = LocalDate.now();
        // 上期 10、本期 5 → delta = -50.0
        when(mapper.countLike(anyList(), any(), any())).thenReturn(List.of(
                new DateCountRow(today.minusDays(45), 10L),  // prev 段（在 currFrom 之前）
                new DateCountRow(today, 5L)                  // curr 段
        ));
        when(mapper.countBrowse(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countCollect(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countComment(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countScore(anyList(), any(), any())).thenReturn(Collections.emptyList());

        UploadImpactVO vo = service.getUploadImpact(5L, "30d");

        assertThat(vo.getDelta().getLike().doubleValue()).isEqualTo(-50.0);
    }

    @Test
    void getUploadImpact_rangeAll_skipsEventQueriesAndReturnsNullDelta() {
        bypassCache();
        when(mapper.selectSitesBySubmitter(6L)).thenReturn(List.of(
                site(501L, 1234L, 100L, 50L, 30L, 10L)
        ));

        UploadImpactVO vo = service.getUploadImpact(6L, "all");

        assertThat(vo.getRange()).isEqualTo("all");
        assertThat(vo.getPoints()).isEmpty();
        assertThat(vo.getDelta().getBrowse()).isNull();
        assertThat(vo.getDelta().getLike()).isNull();
        assertThat(vo.getDelta().getCollect()).isNull();
        assertThat(vo.getDelta().getComment()).isNull();
        assertThat(vo.getDelta().getScore()).isNull();
        // rangeCounts == totals
        assertThat(vo.getRangeCounts().getBrowse()).isEqualTo(1234L);
        assertThat(vo.getRangeCounts().getLike()).isEqualTo(100L);
        // 不查事件表
        verify(mapper, never()).countBrowse(anyList(), any(), any());
        verify(mapper, never()).countLike(anyList(), any(), any());
        verify(mapper, never()).countCollect(anyList(), any(), any());
        verify(mapper, never()).countComment(anyList(), any(), any());
        verify(mapper, never()).countScore(anyList(), any(), any());
    }

    @Test
    void getUploadImpact_invalidRange_throws400() {
        // 不需 mock cache：validateRange 在 cache lookup 之前
        assertThatThrownBy(() -> service.getUploadImpact(7L, "365d"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("range 仅支持");
        verifyNoInteractions(mapper, redisCache);
    }

    @Test
    void getUploadImpact_cacheHit_doesNotInvokeMapper() {
        // 直接返回预构建 VO，不调用 supplier
        UploadImpactVO cached = UploadImpactVO.builder()
                .range("30d").websiteCount(0)
                .totals(zeroMetricVO()).rangeCounts(zeroMetricVO())
                .delta(UploadImpactVO.DeltaGroupVO.builder().build())
                .points(Collections.emptyList())
                .build();
        when(redisCache.getOrLoad(anyString(), eq(UploadImpactVO.class), anyLong(), anyLong(), any()))
                .thenReturn(cached);

        UploadImpactVO vo = service.getUploadImpact(8L, "30d");

        assertThat(vo).isSameAs(cached);
        verifyNoInteractions(mapper);
        verify(redisCache, times(1)).getOrLoad(anyString(), eq(UploadImpactVO.class), anyLong(), anyLong(), any());
    }

    // -------------------- helpers --------------------

    /** 让 RedisCache 直接执行 supplier，绕过缓存 */
    @SuppressWarnings("unchecked")
    private void bypassCache() {
        when(redisCache.getOrLoad(anyString(), eq(UploadImpactVO.class), anyLong(), anyLong(), any()))
                .thenAnswer(inv -> ((Supplier<UploadImpactVO>) inv.getArgument(4)).get());
    }

    private void stubAllEmpty() {
        when(mapper.countBrowse(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countLike(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countCollect(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countComment(anyList(), any(), any())).thenReturn(Collections.emptyList());
        when(mapper.countScore(anyList(), any(), any())).thenReturn(Collections.emptyList());
    }

    private WebsiteCounterRow site(long id, long browse, long like, long collect, long comment, long score) {
        return WebsiteCounterRow.builder()
                .id(id)
                .clickCount((int) browse)
                .likeCount((int) like)
                .collectCount((int) collect)
                .commentCount((int) comment)
                .scoreCount((int) score)
                .build();
    }

    private void assertMetricAllZero(UploadImpactVO.MetricGroupVO m) {
        assertThat(m.getBrowse()).isZero();
        assertThat(m.getLike()).isZero();
        assertThat(m.getCollect()).isZero();
        assertThat(m.getComment()).isZero();
        assertThat(m.getScore()).isZero();
    }

    private UploadImpactVO.MetricGroupVO zeroMetricVO() {
        return UploadImpactVO.MetricGroupVO.builder()
                .browse(0L).like(0L).collect(0L).comment(0L).score(0L).build();
    }
}
