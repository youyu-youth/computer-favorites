package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.userstats.ProfilePublicUserVO;
import com.yyyouth.model.vo.userstats.ProfilePublicVO;
import com.yyyouth.model.vo.userstats.UploadImpactVO;
import com.yyyouth.service.user.userstats.ProfileDashboardService;
import com.yyyouth.service.user.userstats.ProfilePublicService;
import com.yyyouth.service.user.userstats.UploadImpactService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.ProfilePublicController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * {@link ProfilePublicController} upload-impact section 集成测试。
 *
 * 验证：
 *  - section=upload-impact 走 {@link UploadImpactService} 而非 {@link ProfileDashboardService}；
 *  - section=unknown 走 default 文案（含 upload-impact）；
 *  - 用户不存在/不可见时由 {@link ProfilePublicService#getPublicProfile} 抛业务异常，全局映射保留。
 *
 * 限制：未启动 AOP，故 RateLimit 与 AuditLog 不在此测试覆盖范围。
 */
@ExtendWith(MockitoExtension.class)
class ProfilePublicControllerUploadImpactTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private ProfilePublicService profilePublicService;

    @Mock
    private ProfileDashboardService profileDashboardService;

    @Mock
    private UploadImpactService uploadImpactService;

    @InjectMocks
    private ProfilePublicController controller;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void publicUploadImpact_happyPath_routesToUploadImpactService() throws Exception {
        Long targetUserId = 9527L;
        ProfilePublicVO publicVO = ProfilePublicVO.builder()
                .user(ProfilePublicUserVO.builder().id(targetUserId).username("alice").build())
                .build();
        when(profilePublicService.getPublicProfile(eq("alice"), isNull())).thenReturn(publicVO);
        when(profilePublicService.checkDashboardAccess(eq(targetUserId), isNull(), anyBoolean()))
                .thenReturn(new ProfilePublicService.ProfileVisibilityCheckResult(
                        false, "public", 1, 1));

        UploadImpactVO vo = UploadImpactVO.builder()
                .range("7d").websiteCount(2)
                .totals(metric(800L, 50L, 20L, 10L, 5L))
                .rangeCounts(metric(40L, 5L, 2L, 1L, 0L))
                .delta(UploadImpactVO.DeltaGroupVO.builder().build())
                .points(Collections.emptyList())
                .build();
        when(uploadImpactService.getUploadImpact(eq(targetUserId), eq("7d"))).thenReturn(vo);

        mockMvc.perform(get("/api/user/profile/public/alice/dashboard/upload-impact")
                        .param("range", "7d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.range").value("7d"))
                .andExpect(jsonPath("$.data.totals.browse").value(800))
                .andExpect(jsonPath("$.data.rangeCounts.like").value(5));

        verify(uploadImpactService).getUploadImpact(eq(targetUserId), eq("7d"));
        verify(profileDashboardService, never()).getOverview(eq(targetUserId));
    }

    @Test
    void publicUploadImpact_userNotFound_returnsBusinessError() throws Exception {
        when(profilePublicService.getPublicProfile(eq("ghost"), isNull()))
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/api/user/profile/public/ghost/dashboard/upload-impact")
                        .param("range", "30d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND))
                .andExpect(jsonPath("$.msg").value("用户不存在"));
        verify(uploadImpactService, never()).getUploadImpact(eq(9527L), eq("30d"));
    }

    @Test
    void publicSection_unknown_default_includesUploadImpactInHint() throws Exception {
        Long targetUserId = 9527L;
        ProfilePublicVO publicVO = ProfilePublicVO.builder()
                .user(ProfilePublicUserVO.builder().id(targetUserId).username("alice").build())
                .build();
        when(profilePublicService.getPublicProfile(eq("alice"), isNull())).thenReturn(publicVO);
        when(profilePublicService.checkDashboardAccess(eq(targetUserId), isNull(), anyBoolean()))
                .thenReturn(new ProfilePublicService.ProfileVisibilityCheckResult(
                        false, "public", 1, 1));

        mockMvc.perform(get("/api/user/profile/public/alice/dashboard/typo-section"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg",
                        org.hamcrest.Matchers.containsString("upload-impact")));
    }

    private UploadImpactVO.MetricGroupVO metric(long b, long l, long c, long cm, long s) {
        return UploadImpactVO.MetricGroupVO.builder()
                .browse(b).like(l).collect(c).comment(cm).score(s).build();
    }
}
