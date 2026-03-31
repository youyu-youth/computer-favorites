package com.yyyouth.web.admin;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.admin.auth.AdminAuthenticationService;
import com.yyyouth.service.admin.auth.AdminSessionService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.admin.AdminAuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员认证接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminAuthControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private AdminAuthenticationService adminAuthenticationService;

    @Mock
    private AdminSessionService adminSessionService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 管理员登录应返回 token 和管理员基础信息
     */
    @Test
    void shouldReturnLoginDataWhenAdminLoginSuccess() throws Exception {
        AuthUserVO authUserVO = new AuthUserVO();
        authUserVO.setUserId(9001L);
        authUserVO.setUsername("admin");
        authUserVO.setNickname("系统管理员");

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenName("satoken");
        authLoginVO.setTokenValue("admin-token-001");
        authLoginVO.setExpireTime(LocalDateTime.of(2026, 3, 31, 12, 0));
        authLoginVO.setUserInfo(authUserVO);

        when(adminAuthenticationService.login(any(AuthLoginDTO.class), anyString())).thenReturn(authLoginVO);

        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("Admin@123456");
        loginDTO.setDeviceType("web");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("admin-token-001"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(9001L));
    }

    /**
     * 会话续期应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenRenewSession() throws Exception {
        mockMvc.perform(put("/api/admin/auth/session/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("续期成功"));
        verify(adminSessionService).renewSession();
    }

    /**
     * 退出接口应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenLogout() throws Exception {
        mockMvc.perform(delete("/api/admin/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("退出成功"));
        verify(adminSessionService).logout();
    }

    /**
     * 未登录访问会话接口应返回未授权错误
     */
    @Test
    void shouldReturnUnauthorizedWhenRequestCurrentSessionWithoutToken() throws Exception {
        when(adminSessionService.currentSession())
                .thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "admin", "无token"));

        mockMvc.perform(get("/api/admin/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }

    /**
     * 已登录访问会话接口应返回会话信息
     */
    @Test
    void shouldReturnCurrentSessionWhenAuthorized() throws Exception {
        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(9001L);
        sessionVO.setTokenValue("admin-token-001");
        sessionVO.setDeviceType("web");
        sessionVO.setTimeoutSeconds(1800L);
        sessionVO.setExpireTime(LocalDateTime.of(2026, 3, 31, 12, 0));

        when(adminSessionService.currentSession()).thenReturn(sessionVO);

        mockMvc.perform(get("/api/admin/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("admin-token-001"));
        verify(adminSessionService).currentSession();
    }
}
