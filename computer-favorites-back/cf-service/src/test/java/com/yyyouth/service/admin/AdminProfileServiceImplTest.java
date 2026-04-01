package com.yyyouth.service.admin;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminPasswordUpdateDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.service.admin.profile.impl.AdminProfileServiceImpl;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员资料服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminProfileServiceImplTest {

    private static final Long ADMIN_ID = 9001L;

    @Mock
    private AdminAccountMapper adminAccountMapper;

    @InjectMocks
    private AdminProfileServiceImpl adminProfileService;

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
     * 修改密码成功时应更新密码并退出管理员会话
     */
    @Test
    void shouldUpdatePasswordAndLogoutWhenPayloadValid() {
        AdminAccount currentAdmin = new AdminAccount();
        currentAdmin.setId(ADMIN_ID);
        currentAdmin.setStatus(1);
        currentAdmin.setDeleted(0);
        currentAdmin.setPasswordHash(new BCryptPasswordEncoder().encode("Admin@123456"));

        AdminPasswordUpdateDTO updateDTO = new AdminPasswordUpdateDTO();
        updateDTO.setCurrentPassword("Admin@123456");
        updateDTO.setNewPassword("Admin@654321");
        updateDTO.setConfirmPassword("Admin@654321");

        when(adminAccountMapper.selectOne(any())).thenReturn(currentAdmin);
        when(adminAccountMapper.update(eq(null), any())).thenReturn(1);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::checkLogin).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);

            adminProfileService.updateLoginAdminPassword(updateDTO);

            verify(adminAccountMapper).update(eq(null), any());
            stpAdminUtilMock.verify(StpAdminUtil::logout);
        }
    }

    /**
     * 当前密码错误时应抛出业务异常
     */
    @Test
    void shouldThrowBusinessExceptionWhenCurrentPasswordInvalid() {
        AdminAccount currentAdmin = new AdminAccount();
        currentAdmin.setId(ADMIN_ID);
        currentAdmin.setStatus(1);
        currentAdmin.setDeleted(0);
        currentAdmin.setPasswordHash(new BCryptPasswordEncoder().encode("Admin@123456"));

        AdminPasswordUpdateDTO updateDTO = new AdminPasswordUpdateDTO();
        updateDTO.setCurrentPassword("wrong-password");
        updateDTO.setNewPassword("Admin@654321");
        updateDTO.setConfirmPassword("Admin@654321");

        when(adminAccountMapper.selectOne(any())).thenReturn(currentAdmin);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::checkLogin).thenAnswer(invocation -> null);
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);

            assertThatThrownBy(() -> adminProfileService.updateLoginAdminPassword(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorCode.CHANGE_PASSWORD_CURRENT_INVALID.getCode());
        }
    }
}
