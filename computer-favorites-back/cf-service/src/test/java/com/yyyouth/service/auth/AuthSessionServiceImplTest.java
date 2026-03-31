package com.yyyouth.service.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.user.auth.impl.AuthSessionServiceImpl;
import com.yyyouth.service.mapper.user.auth.UserSessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 认证会话服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthSessionServiceImplTest {

    private static final Long USER_ID = 1001L;

    private static final String TOKEN_VALUE = "token-test-001";

    @Mock
    private UserSessionMapper userSessionMapper;

    @Mock
    private SaSession tokenSession;

    @InjectMocks
    private AuthSessionServiceImpl authSessionService;

    /**
     * 续期时应刷新会话
     */
    @Test
    void shouldRenewSessionAndUpdateDb() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(1200L);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(() -> StpUtil.renewTimeout(1200L)).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::updateLastActiveToNow).thenAnswer(invocation -> null);

            authSessionService.renewSession();

            verify(userSessionMapper, times(1)).update(eq(null), any());
        }
    }

    /**
     * 退出时应更新会话状态
     */
    @Test
    void shouldLogoutAndInvalidateSession() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::logout).thenAnswer(invocation -> null);

            authSessionService.logout();

            verify(userSessionMapper, times(1)).update(eq(null), any());
        }
    }

    /**
     * 查询当前会话时应返回会话信息
     */
    @Test
    void shouldReturnCurrentSession() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(1800L);
            stpUtilMock.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            org.mockito.Mockito.when(tokenSession.get("deviceType")).thenReturn("web");

            AuthSessionVO sessionVO = authSessionService.currentSession();

            assertThat(sessionVO.getUserId()).isEqualTo(USER_ID);
            assertThat(sessionVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(sessionVO.getDeviceType()).isEqualTo("web");
            assertThat(sessionVO.getTimeoutSeconds()).isEqualTo(1800L);
        }
    }
}
