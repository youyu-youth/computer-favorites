package com.yyyouth.service.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.auth.UserSession;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.auth.UserSessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.time.Duration;

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
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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
     * 登录成功时应返回 token 并写入会话表及 Redis
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
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

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

            verify(valueOperations).set(eq(RedisConstant.AUTH_SESSION_TOKEN + TOKEN_VALUE), any(String.class), eq(Duration.ofSeconds(1800L)));
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
     * 续期时应刷新会话并更新 Redis
     */
    @Test
    void shouldRenewSessionAndUpdateRedis() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(tokenSession.get("deviceType")).thenReturn("web");

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
            verify(valueOperations, times(1)).set(eq(RedisConstant.AUTH_SESSION_TOKEN + TOKEN_VALUE), any(String.class), eq(Duration.ofSeconds(1200L)));
        }
    }

    /**
     * 退出时应更新会话状态并删除 Redis 缓存
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
            verify(stringRedisTemplate, times(1)).delete(RedisConstant.AUTH_SESSION_TOKEN + TOKEN_VALUE);
        }
    }
}
