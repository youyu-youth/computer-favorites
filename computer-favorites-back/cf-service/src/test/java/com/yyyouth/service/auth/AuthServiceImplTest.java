package com.yyyouth.service.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.EmailUtils;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthPasswordChangeDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.service.auth.impl.AuthenticationServiceImpl;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
 * 认证核心服务单元测试
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
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private EmailUtils emailUtils;

    @Mock
    private SaSession tokenSession;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

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

            AuthLoginVO loginVO = authenticationService.login(loginDTO);

            assertThat(loginVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(loginVO.getTokenName()).isEqualTo("satoken");
            assertThat(loginVO.getUserInfo().getUserId()).isEqualTo(USER_ID);

            ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
            verify(userSessionMapper).insert(sessionCaptor.capture());
            UserSession insertedSession = sessionCaptor.getValue();
            assertThat(insertedSession.getUserId()).isEqualTo(USER_ID);
            assertThat(insertedSession.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(insertedSession.getOperationSource()).isEqualTo("login");

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

        assertThatThrownBy(() -> authenticationService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIAL.getCode());
    }

    /**
     * 邮箱验证码登录成功时应返回 token 并清理验证码
     */
    @Test
    void shouldLoginByEmailCodeAndClearCodeWhenCodeValid() {
        AuthLoginEmailCodeDTO loginEmailCodeDTO = new AuthLoginEmailCodeDTO();
        loginEmailCodeDTO.setEmail("tester@test.com");
        loginEmailCodeDTO.setEmailCode("123456");
        loginEmailCodeDTO.setDeviceType("web");

        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setEmail("tester@test.com");
        userAccount.setUsername("tester");
        userAccount.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(userSessionMapper.selectOne(any())).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("123456");

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.login(eq(USER_ID), any(SaLoginParameter.class))).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenSession).thenReturn(tokenSession);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpUtilMock.when(StpUtil::getTokenTimeout).thenReturn(1800L);
            stpUtilMock.when(StpUtil::getTokenName).thenReturn("satoken");

            AuthLoginVO loginVO = authenticationService.loginByEmailCode(loginEmailCodeDTO);

            assertThat(loginVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            verify(stringRedisTemplate).delete("email:code:login:tester@test.com");
        }
    }

    /**
     * 发送登录验证码成功时应写入 Redis
     */
    @Test
    void shouldSendLoginEmailCodeAndWriteRedisWhenEmailValid() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setEmail("tester@test.com");
        userAccount.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(stringRedisTemplate.hasKey(any())).thenReturn(false);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        when(emailUtils.sendGeneralEmail(any(), any(), eq("tester@test.com"))).thenReturn(true);

        authenticationService.sendLoginEmailCode("tester@test.com");

        verify(valueOperations).set(eq("email:code:login:tester@test.com"), any(), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    /**
     * 发送修改密码验证码成功时应写入 Redis
     */
    @Test
    void shouldSendResetEmailCodeAndWriteRedisWhenEmailMatched() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setEmail("tester@test.com");
        userAccount.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(stringRedisTemplate.hasKey(any())).thenReturn(false);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        when(emailUtils.sendGeneralEmail(any(), any(), eq("tester@test.com"))).thenReturn(true);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            authenticationService.sendPasswordResetEmailCode("tester@test.com");
        }

        verify(valueOperations).set(eq("email:code:reset:tester@test.com"), any(), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    /**
     * 修改密码成功时应更新密码并清理验证码
     */
    @Test
    void shouldChangePasswordAndClearCodeWhenParamValid() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(USER_ID);
        userAccount.setEmail("tester@test.com");
        userAccount.setStatus(1);
        userAccount.setPasswordHash(new BCryptPasswordEncoder().encode("Old@123456"));
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(userAccountMapper.update(eq(null), any())).thenReturn(1);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("email:code:reset:tester@test.com"))).thenReturn("123456");

        AuthPasswordChangeDTO changeDTO = new AuthPasswordChangeDTO();
        changeDTO.setCurrentPassword("Old@123456");
        changeDTO.setNewPassword("New@123456");
        changeDTO.setConfirmPassword("New@123456");
        changeDTO.setEmail("tester@test.com");
        changeDTO.setEmailCode("123456");

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            stpUtilMock.when(StpUtil::logout).thenAnswer(invocation -> null);
            authenticationService.changePassword(changeDTO);
        }

        verify(userAccountMapper, times(1)).update(eq(null), any());
        verify(userSessionMapper, times(1)).update(eq(null), any());
        verify(stringRedisTemplate).delete("email:code:reset:tester@test.com");
    }

    /**
     * 注册成功时应写入用户、资料与设置
     */
    @Test
    void shouldRegisterAndInitProfileAndSetting() {
        AuthRegisterDTO registerDTO = new AuthRegisterDTO();
        registerDTO.setUsername("new_user");
        registerDTO.setEmail("new-user@test.com");
        registerDTO.setPassword("123456");
        registerDTO.setEmailCode("123456");
        when(userAccountMapper.selectOne(any())).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("123456");

        authenticationService.register(registerDTO);

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountMapper).insert(accountCaptor.capture());
        UserAccount insertedAccount = accountCaptor.getValue();
        assertThat(insertedAccount.getEmail()).isEqualTo("new-user@test.com");
        assertThat(insertedAccount.getUsername()).isEqualTo("new_user");
        assertThat(insertedAccount.getPasswordHash()).isNotBlank();
        assertThat(new BCryptPasswordEncoder().matches("123456", insertedAccount.getPasswordHash())).isTrue();

        verify(userProfileMapper, times(1)).insert(any(UserProfile.class));
        verify(userSettingMapper, times(1)).insert(any(UserSetting.class));
    }

    /**
     * 注册邮箱重复时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenRegisterEmailExists() {
        AuthRegisterDTO registerDTO = new AuthRegisterDTO();
        registerDTO.setUsername("exist_user");
        registerDTO.setEmail("exist@test.com");
        registerDTO.setPassword("123456");
        registerDTO.setEmailCode("123456");
        UserAccount existAccount = new UserAccount();
        existAccount.setId(USER_ID);
        when(userAccountMapper.selectOne(any())).thenReturn(existAccount);

        assertThatThrownBy(() -> authenticationService.register(registerDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.REGISTER_EMAIL_EXISTS.getCode());
    }
}
