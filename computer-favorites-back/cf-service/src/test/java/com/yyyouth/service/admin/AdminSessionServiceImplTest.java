package com.yyyouth.service.admin;

import cn.dev33.satoken.session.SaSession;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.admin.impl.AdminSessionServiceImpl;
import com.yyyouth.service.auth.support.StpAdminUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员会话服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminSessionServiceImplTest {

    private static final Long ADMIN_ID = 9001L;

    private static final String TOKEN_VALUE = "admin-token-001";

    @Mock
    private SaSession tokenSession;

    @InjectMocks
    private AdminSessionServiceImpl adminSessionService;

    /**
     * 续期时应刷新管理员会话
     */
    @Test
    void shouldRenewSessionWhenAdminLoggedIn() {
        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::checkLogin).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::getTokenTimeout).thenReturn(1200L);
            stpAdminUtilMock.when(() -> StpAdminUtil.renewTimeout(1200L)).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::updateLastActiveToNow).thenAnswer(invocation -> null);

            adminSessionService.renewSession();

            stpAdminUtilMock.verify(() -> StpAdminUtil.renewTimeout(1200L));
            stpAdminUtilMock.verify(StpAdminUtil::updateLastActiveToNow);
        }
    }

    /**
     * 查询当前会话时应返回管理员会话信息
     */
    @Test
    void shouldReturnCurrentSessionWhenAdminLoggedIn() {
        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::checkLogin).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            stpAdminUtilMock.when(StpAdminUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpAdminUtilMock.when(StpAdminUtil::getTokenTimeout).thenReturn(1800L);
            stpAdminUtilMock.when(StpAdminUtil::getTokenSession).thenReturn(tokenSession);
            org.mockito.Mockito.when(tokenSession.get("deviceType")).thenReturn("web");

            AuthSessionVO sessionVO = adminSessionService.currentSession();

            assertThat(sessionVO.getUserId()).isEqualTo(ADMIN_ID);
            assertThat(sessionVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(sessionVO.getDeviceType()).isEqualTo("web");
            assertThat(sessionVO.getTimeoutSeconds()).isEqualTo(1800L);
            verify(tokenSession).get("deviceType");
        }
    }

    /**
     * 退出时应调用管理员退出逻辑
     */
    @Test
    void shouldLogoutWhenAdminLoggedIn() {
        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::checkLogin).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::logout).thenAnswer(invocation -> null);

            adminSessionService.logout();

            stpAdminUtilMock.verify(StpAdminUtil::logout);
        }
    }
}
