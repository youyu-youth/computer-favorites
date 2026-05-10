package com.yyyouth.web.user;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.service.user.userstats.ProfileDashboardService;
import com.yyyouth.service.user.userstats.UploadImpactService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.ProfileDashboardController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * {@link ProfileDashboardController#uploadImpact} 集成测试。
 *
 * 限制：standalone MockMvc 不启动 Sa-Token / RateLimit / AuditLog AOP，
 * 故此处用 {@link MockedStatic} 拦截 {@link StpUtil}，仅验证业务流转 + 异常映射；
 * 限流 429 与未登录 401 由 AOP 链路覆盖，不在此层测试。
 */
@ExtendWith(MockitoExtension.class)
class ProfileDashboardControllerUploadImpactTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private MockedStatic<StpUtil> stpUtilMock;

    @Mock
    private ProfileDashboardService profileDashboardService;

    @Mock
    private UploadImpactService uploadImpactService;

    @InjectMocks
    private ProfileDashboardController controller;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        stpUtilMock = Mockito.mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
    }

    @Test
    void uploadImpact_happyPath_returnsVoShape() throws Exception {
        UploadImpactVO vo = UploadImpactVO.builder()
                .range("30d").websiteCount(3)
                .totals(metric(1234L, 100L, 50L, 30L, 10L))
                .rangeCounts(metric(50L, 5L, 2L, 1L, 0L))
                .delta(UploadImpactVO.DeltaGroupVO.builder().build())
                .points(Collections.emptyList())
                .build();
        when(uploadImpactService.getUploadImpact(eq(1001L), eq("30d"))).thenReturn(vo);

        mockMvc.perform(get("/api/user/profile/dashboard/upload-impact").param("range", "30d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.range").value("30d"))
                .andExpect(jsonPath("$.data.websiteCount").value(3))
                .andExpect(jsonPath("$.data.totals.browse").value(1234))
                .andExpect(jsonPath("$.data.rangeCounts.like").value(5));
        verify(uploadImpactService).getUploadImpact(eq(1001L), eq("30d"));
    }

    @Test
    void uploadImpact_defaultRange_passes30dToService() throws Exception {
        UploadImpactVO vo = UploadImpactVO.builder()
                .range("30d").websiteCount(0)
                .totals(metric(0L, 0L, 0L, 0L, 0L))
                .rangeCounts(metric(0L, 0L, 0L, 0L, 0L))
                .delta(UploadImpactVO.DeltaGroupVO.builder().build())
                .points(Collections.emptyList())
                .build();
        when(uploadImpactService.getUploadImpact(eq(1001L), eq("30d"))).thenReturn(vo);

        mockMvc.perform(get("/api/user/profile/dashboard/upload-impact"))
                .andExpect(status().isOk());
        verify(uploadImpactService).getUploadImpact(eq(1001L), eq("30d"));
    }

    @Test
    void uploadImpact_invalidRange_returnsBusinessError() throws Exception {
        when(uploadImpactService.getUploadImpact(eq(1001L), eq("365d")))
                .thenThrow(new BusinessException(HttpStatus.BAD_REQUEST,
                        "range 仅支持 7d / 30d / 90d / all"));

        mockMvc.perform(get("/api/user/profile/dashboard/upload-impact").param("range", "365d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("range 仅支持 7d / 30d / 90d / all"));
    }

    @Test
    void uploadImpact_rangeAll_returnsAllVo() throws Exception {
        UploadImpactVO vo = UploadImpactVO.builder()
                .range("all").websiteCount(2)
                .totals(metric(9999L, 200L, 80L, 40L, 15L))
                .rangeCounts(metric(9999L, 200L, 80L, 40L, 15L))
                .delta(UploadImpactVO.DeltaGroupVO.builder().build())
                .points(Collections.emptyList())
                .build();
        when(uploadImpactService.getUploadImpact(eq(1001L), eq("all"))).thenReturn(vo);

        mockMvc.perform(get("/api/user/profile/dashboard/upload-impact").param("range", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.range").value("all"))
                .andExpect(jsonPath("$.data.totals.browse").value(9999))
                .andExpect(jsonPath("$.data.rangeCounts.browse").value(9999))
                .andExpect(jsonPath("$.data.points.length()").value(0));
    }

    private UploadImpactVO.MetricGroupVO metric(long b, long l, long c, long cm, long s) {
        return UploadImpactVO.MetricGroupVO.builder()
                .browse(b).like(l).collect(c).comment(cm).score(s).build();
    }
}
