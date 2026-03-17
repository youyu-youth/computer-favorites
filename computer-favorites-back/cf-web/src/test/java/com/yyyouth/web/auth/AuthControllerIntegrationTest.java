package com.yyyouth.web.auth;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.auth.impl.AuthService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.auth.AuthController;
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
 * @date 2026-03-16
 *
 * 认证接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 登录接口应返回 token 与用户基础信息
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnLoginDataWhenLoginSuccess() throws Exception {
        AuthUserVO authUserVO = new AuthUserVO();
        authUserVO.setUserId(1001L);
        authUserVO.setUsername("tester");
        authUserVO.setNickname("测试用户");

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenName("satoken");
        authLoginVO.setTokenValue("token-web-001");
        authLoginVO.setExpireTime(LocalDateTime.of(2026, 3, 16, 12, 0));
        authLoginVO.setUserInfo(authUserVO);
        when(authService.login(any(AuthLoginDTO.class))).thenReturn(authLoginVO);

        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("tester");
        loginDTO.setPassword("123456");
        loginDTO.setDeviceType("web");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("token-web-001"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(1001L));
    }

    /**
     * 未登录访问受保护接口应返回统一未授权响应
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnUnauthorizedWhenRequestCurrentSessionWithoutToken() throws Exception {
        when(authService.currentSession()).thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "login", "无token"));

        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }

    /**
     * 已授权访问受保护接口应返回会话信息
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnCurrentSessionWhenAuthorized() throws Exception {
        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(1001L);
        sessionVO.setTokenValue("token-web-001");
        sessionVO.setDeviceType("web");
        sessionVO.setTimeoutSeconds(1800L);
        sessionVO.setExpireTime(LocalDateTime.of(2026, 3, 16, 12, 0));
        when(authService.currentSession()).thenReturn(sessionVO);

        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("token-web-001"));
        verify(authService).currentSession();
    }

    /**
     * 续期接口应返回统一成功响应
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnSuccessWhenRenewSession() throws Exception {
        mockMvc.perform(put("/api/auth/session/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("续期成功"));
        verify(authService).renewSession();
    }

    /**
     * 退出后再次访问会话接口应返回未授权
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnUnauthorizedWhenAccessAfterLogout() throws Exception {
        mockMvc.perform(delete("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("退出成功"));
        verify(authService).logout();

        when(authService.currentSession()).thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "login", "无token"));
        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    /**
     * 登录参数非法时应返回校验错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBadRequestWhenLoginParamInvalid() throws Exception {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("");
        loginDTO.setDeviceType("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST));
    }
}
