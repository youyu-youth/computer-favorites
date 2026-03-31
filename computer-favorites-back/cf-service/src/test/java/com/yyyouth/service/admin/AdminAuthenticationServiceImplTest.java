package com.yyyouth.service.admin;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.service.admin.impl.AdminAuthenticationServiceImpl;
import com.yyyouth.service.auth.support.StpAdminUtil;
import com.yyyouth.service.mapper.admin.AdminAccountMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthenticationServiceImplTest {

    private static final Long ADMIN_ID = 9001L;

    private static final String TOKEN_VALUE = "admin-token-001";

    @Mock
    private AdminAccountMapper adminAccountMapper;

    @Mock
    private SaSession tokenSession;

    @InjectMocks
    private AdminAuthenticationServiceImpl adminAuthenticationService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, AdminAccount.class);
    }

    /**
     * 登录成功时应返回 token 并更新管理员登录审计信息
     */
    @Test
    void shouldLoginAndUpdateAuditWhenCredentialValid() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("Admin@123456");
        loginDTO.setDeviceType("web");

        AdminAccount adminAccount = new AdminAccount();
        adminAccount.setId(ADMIN_ID);
        adminAccount.setUsername("admin");
        adminAccount.setNickname("系统管理员");
        adminAccount.setStatus(1);
        adminAccount.setPasswordHash(new BCryptPasswordEncoder().encode("Admin@123456"));

        when(adminAccountMapper.selectOne(any())).thenReturn(adminAccount);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(() -> StpAdminUtil.login(eq(ADMIN_ID), any(SaLoginParameter.class)))
                    .thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::getTokenSession).thenReturn(tokenSession);
            stpAdminUtilMock.when(StpAdminUtil::getTokenValue).thenReturn(TOKEN_VALUE);
            stpAdminUtilMock.when(StpAdminUtil::getTokenTimeout).thenReturn(1800L);
            stpAdminUtilMock.when(StpAdminUtil::getTokenName).thenReturn("satoken");

            AuthLoginVO loginVO = adminAuthenticationService.login(loginDTO, "127.0.0.1");

            assertThat(loginVO.getTokenValue()).isEqualTo(TOKEN_VALUE);
            assertThat(loginVO.getTokenName()).isEqualTo("satoken");
            assertThat(loginVO.getUserInfo().getUserId()).isEqualTo(ADMIN_ID);

            verify(adminAccountMapper).update(eq(null), any());
            verify(tokenSession).set("deviceType", "web");
        }
    }

    /**
     * 密码错误时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenPasswordInvalid() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrong-password");
        loginDTO.setDeviceType("web");

        AdminAccount adminAccount = new AdminAccount();
        adminAccount.setId(ADMIN_ID);
        adminAccount.setUsername("admin");
        adminAccount.setStatus(1);
        adminAccount.setPasswordHash(new BCryptPasswordEncoder().encode("Admin@123456"));

        when(adminAccountMapper.selectOne(any())).thenReturn(adminAccount);

        assertThatThrownBy(() -> adminAuthenticationService.login(loginDTO, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIAL.getCode());
    }

    /**
     * 管理员不存在时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenAdminNotFound() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("Admin@123456");
        loginDTO.setDeviceType("web");

        when(adminAccountMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> adminAuthenticationService.login(loginDTO, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIAL.getCode());
    }
}
