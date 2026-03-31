package com.yyyouth.web.auth;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthPasswordChangeDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.user.auth.AuthSessionService;
import com.yyyouth.service.user.auth.AuthenticationService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserAuthController;
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
    private AuthenticationService authenticationService;

    @Mock
    private AuthSessionService authSessionService;

    @InjectMocks
    private UserAuthController userAuthController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 登录接口应返回 token 与用户基础信息
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
        when(authenticationService.login(any(AuthLoginDTO.class))).thenReturn(authLoginVO);

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
     * 邮箱验证码登录接口应返回 token 与用户基础信息
     */
    @Test
    void shouldReturnLoginDataWhenLoginByEmailCodeSuccess() throws Exception {
        AuthUserVO authUserVO = new AuthUserVO();
        authUserVO.setUserId(1001L);
        authUserVO.setUsername("tester");
        authUserVO.setNickname("测试用户");

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenName("satoken");
        authLoginVO.setTokenValue("token-web-002");
        authLoginVO.setExpireTime(LocalDateTime.of(2026, 3, 16, 12, 0));
        authLoginVO.setUserInfo(authUserVO);
        when(authenticationService.loginByEmailCode(any(AuthLoginEmailCodeDTO.class))).thenReturn(authLoginVO);

        AuthLoginEmailCodeDTO loginEmailCodeDTO = new AuthLoginEmailCodeDTO();
        loginEmailCodeDTO.setEmail("tester@test.com");
        loginEmailCodeDTO.setEmailCode("123456");
        loginEmailCodeDTO.setDeviceType("web");

        mockMvc.perform(post("/api/auth/login/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginEmailCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("token-web-002"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(1001L));
    }

    /**
     * 发送登录验证码接口应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenSendLoginCode() throws Exception {
        mockMvc.perform(post("/api/auth/login/code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("验证码发送成功"));
        verify(authenticationService).sendLoginEmailCode("tester@test.com");
    }

    /**
     * 发送修改密码验证码接口应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenSendPasswordCode() throws Exception {
        mockMvc.perform(post("/api/auth/password/code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("验证码发送成功"));
        verify(authenticationService).sendPasswordResetEmailCode("tester@test.com");
    }

    /**
     * 修改密码接口应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenChangePassword() throws Exception {
        AuthPasswordChangeDTO changeDTO = new AuthPasswordChangeDTO();
        changeDTO.setCurrentPassword("Old@123456");
        changeDTO.setNewPassword("New@123456");
        changeDTO.setConfirmPassword("New@123456");
        changeDTO.setEmail("tester@test.com");
        changeDTO.setEmailCode("123456");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("密码修改成功"));
        verify(authenticationService).changePassword(any(AuthPasswordChangeDTO.class));
    }

    /**
     * 未登录访问受保护接口应返回统一未授权响应
     */
    @Test
    void shouldReturnUnauthorizedWhenRequestCurrentSessionWithoutToken() throws Exception {
        when(authSessionService.currentSession()).thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "login", "无token"));

        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }

    /**
     * 已授权访问受保护接口应返回会话信息
     */
    @Test
    void shouldReturnCurrentSessionWhenAuthorized() throws Exception {
        AuthSessionVO sessionVO = new AuthSessionVO();
        sessionVO.setUserId(1001L);
        sessionVO.setTokenValue("token-web-001");
        sessionVO.setDeviceType("web");
        sessionVO.setTimeoutSeconds(1800L);
        sessionVO.setExpireTime(LocalDateTime.of(2026, 3, 16, 12, 0));
        when(authSessionService.currentSession()).thenReturn(sessionVO);

        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.tokenValue").value("token-web-001"));
        verify(authSessionService).currentSession();
    }

    /**
     * 续期接口应返回统一成功响应
     */
    @Test
    void shouldReturnSuccessWhenRenewSession() throws Exception {
        mockMvc.perform(put("/api/auth/session/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("续期成功"));
        verify(authSessionService).renewSession();
    }

    /**
     * 退出后再次访问会话接口应返回未授权
     */
    @Test
    void shouldReturnUnauthorizedWhenAccessAfterLogout() throws Exception {
        mockMvc.perform(delete("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("退出成功"));
        verify(authSessionService).logout();

        when(authSessionService.currentSession()).thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "login", "无token"));
        mockMvc.perform(get("/api/auth/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()));
    }

    /**
     * 登录参数非法时应返回校验错误
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
