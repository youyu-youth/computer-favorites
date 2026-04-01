package com.yyyouth.web.admin;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.admin.AdminPasswordUpdateDTO;
import com.yyyouth.service.admin.profile.AdminProfileService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.admin.AdminProfileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员资料接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminProfileControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private AdminProfileService adminProfileService;

    @InjectMocks
    private AdminProfileController adminProfileController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 管理员修改密码成功应返回成功响应
     */
    @Test
    void shouldReturnSuccessWhenUpdateLoginAdminPassword() throws Exception {
        AdminPasswordUpdateDTO updateDTO = new AdminPasswordUpdateDTO();
        updateDTO.setCurrentPassword("Admin@123456");
        updateDTO.setNewPassword("Admin@654321");
        updateDTO.setConfirmPassword("Admin@654321");

        doNothing().when(adminProfileService).updateLoginAdminPassword(any());

        mockMvc.perform(put("/api/admin/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("密码修改成功"));

        verify(adminProfileService).updateLoginAdminPassword(any());
    }

    /**
     * 未登录修改管理员密码应返回未授权
     */
    @Test
    void shouldReturnUnauthorizedWhenUpdateLoginAdminPasswordWithoutLogin() throws Exception {
        AdminPasswordUpdateDTO updateDTO = new AdminPasswordUpdateDTO();
        updateDTO.setCurrentPassword("Admin@123456");
        updateDTO.setNewPassword("Admin@654321");
        updateDTO.setConfirmPassword("Admin@654321");

        doThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "admin", "无token"))
                .when(adminProfileService)
                .updateLoginAdminPassword(any());

        mockMvc.perform(put("/api/admin/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }
}
