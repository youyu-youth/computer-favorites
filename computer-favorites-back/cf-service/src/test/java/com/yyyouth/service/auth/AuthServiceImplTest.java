package com.yyyouth.service.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Long USER_ID = 1001L;

    private static final String TOKEN_VALUE = "token-test-001";

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private UserSessionMapper userSessionMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserSettingMapper userSettingMapper;

    @Mock
    private SaSession tokenSession;

    @InjectMocks
    private AuthServiceImpl authService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserSession.class);
    }

    /**
     * 登录成功时应返回 token 并写入会话表
     */
    @Test
    void shouldLoginAndWriteSessionWhenCredentialValid() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("tester");
        loginDTO.setPassword("123456");
        loginDTO.setDeviceType("web");

        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setUsername("tester");
        userAccount.setNickname("测试用户");
        userAccount.setStatus(1);
        userAccount.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));

        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(userSessionMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.login(eq(USER_ID), any(SaLoginParameter.class))).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(1800L);
            stpUtilMock.when(StpUtil::getTokenName).thenReturn("satoken");

            AuthLoginVO loginVO = authService.login(loginDTO);

            assertThat(loginVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(loginVO.getTokenName()).isEqualTo("satoken");
            assertThat(loginVO.getUserInfo().getUserId()).isEqualTo(USER_ID);

            ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
            verify(userSessionMapper).insert(sessionCaptor.capture());
            UserSession insertedSession = sessionCaptor.getValue();
            assertThat(insertedSession.getUserId()).isEqualTo(USER_ID);
            assertThat(insertedSession.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(insertedSession.getOperationSource()).isEqualTo("login");
            assertThat(insertedSession.getCreateBy()).isEqualTo(String.valueOf(USER_ID));
            assertThat(insertedSession.getUpdateBy()).isEqualTo(String.valueOf(USER_ID));
            assertThat(insertedSession.getCreateTime()).isNotNull();
            assertThat(insertedSession.getUpdateTime()).isNotNull();

            verify(tokenSession).set("deviceType", "web");
        }
    }

    /**
     * 登录密码错误时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenPasswordInvalid() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("tester");
        loginDTO.setPassword("wrong-password");
        loginDTO.setDeviceType("web");

        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setUsername("tester");
        userAccount.setStatus(1);
        userAccount.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);

        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIAL.getCode());
    }

    /**
     * 续期时应刷新会话
     */
    @Test
    void shouldRenewSessionAndUpdateRedis() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(1200L);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            stpUtilMock.when(() -> StpUtil.renewTimeout(1200L)).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::updateLastActiveToNow).thenAnswer(invocation -> null);

            authService.renewSession();

            verify(userSessionMapper, times(1)).update(eq(null), any());
        }
    }

    /**
     * 退出时应更新会话状态
     */
    @Test
    void shouldLogoutAndDeleteRedisSession() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::logout).thenAnswer(invocation -> null);

            authService.logout();

            verify(userSessionMapper, times(1)).update(eq(null), any());
        }
    }

    /**
     * 注册成功时应写入用户、资料与设置
     */
    @Test
    void shouldRegisterAndInitProfileAndSetting() {
        AuthRegisterDTO registerDTO = new AuthRegisterDTO();
        registerDTO.setEmail("new-user@test.com");
        registerDTO.setPassword("123456");
        when(userAccountMapper.selectOne(any())).thenReturn(null);

        authService.register(registerDTO);

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountMapper).insert(accountCaptor.capture());
        UserAccount insertedAccount = accountCaptor.getValue();
        assertThat(insertedAccount.getEmail()).isEqualTo("new-user@test.com");
        assertThat(insertedAccount.getPasswordHash()).isNotBlank();
        assertThat(new BCryptPasswordEncoder().matches("123456", insertedAccount.getPasswordHash())).isTrue();
        assertThat(insertedAccount.getDeleted()).isEqualTo(0);

        verify(userProfileMapper, times(1)).insert(any(UserProfile.class));
        verify(userSettingMapper, times(1)).insert(any(UserSetting.class));
    }

    /**
     * 注册邮箱重复时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenRegisterEmailExists() {
        AuthRegisterDTO registerDTO = new AuthRegisterDTO();
        registerDTO.setEmail("exist@test.com");
        registerDTO.setPassword("123456");
        UserAccount existAccount = new UserAccount();
        existAccount.setId(USER_ID);
        when(userAccountMapper.selectOne(any())).thenReturn(existAccount);

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.REGISTER_EMAIL_EXISTS.getCode());
        verify(userAccountMapper, times(0)).insert(any(UserAccount.class));
        verify(userProfileMapper, times(0)).insert(any(UserProfile.class));
        verify(userSettingMapper, times(0)).insert(any(UserSetting.class));
    }

    /**
     * 续期场景应使用默认续期时长
     */
    @Test
    void shouldUseDefaultTimeoutWhenTokenTimeoutIsNonPositive() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(0L);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            stpUtilMock.when(() -> StpUtil.renewTimeout(3600L)).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::updateLastActiveToNow).thenAnswer(invocation -> null);

            authService.renewSession();

            stpUtilMock.verify(() -> StpUtil.renewTimeout(3600L), times(1));
            verify(userSessionMapper, times(1)).update(eq(null), any());
        }
    }
}
