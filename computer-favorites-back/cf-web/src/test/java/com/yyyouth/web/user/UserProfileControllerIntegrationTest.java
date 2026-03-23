package com.yyyouth.web.user;

import cn.dev33.satoken.exception.NotLoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.model.vo.user.UserBasicInfoVO;
import com.yyyouth.model.vo.user.UserDetailProfileVO;
import com.yyyouth.model.vo.user.UserPreferenceSettingVO;
import com.yyyouth.service.user.UserProfileService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserProfileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户资料接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserProfileControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 已登录查询应返回聚合资料
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnLoginUserProfileWhenAuthorized() throws Exception {
        UserBasicInfoVO userVO = new UserBasicInfoVO();
        userVO.setId(1001L);
        userVO.setUsername("tester");
        userVO.setNickname("测试用户");
        userVO.setEmail("tester@test.com");

        UserDetailProfileVO profileVO = new UserDetailProfileVO();
        profileVO.setGender(1);
        profileVO.setCountry("中国");
        profileVO.setCity("上海");
        profileVO.setGithubUrl("https://github.com/tester");
        profileVO.setGiteeUrl("https://gitee.com/tester");
        profileVO.setOtherRepoLinks("https://gitlab.com/tester");
        profileVO.setBlogUrl("https://blog.tester.com");
        profileVO.setSignature("专注后端与架构");
        profileVO.setHobbyTags("阅读,跑步");
        profileVO.setTechStack("Java,Spring Boot,MySQL");
        profileVO.setFavoriteWebsites("https://spring.io,https://mybatis.plus");
        profileVO.setUploadedWebsites("https://demo.tester.com");
        profileVO.setContribution("256");
        profileVO.setDeleted(0);

        UserPreferenceSettingVO settingVO = new UserPreferenceSettingVO();
        settingVO.setTheme("dark");
        settingVO.setLanguage("zh-CN");
        settingVO.setEmailNotice(1);
        settingVO.setCollectNotice(1);
        settingVO.setCommentNotice(0);
        settingVO.setHomepageStyle("cards");
        settingVO.setPageSize(24);

        LoginUserProfileVO resultVO = new LoginUserProfileVO();
        resultVO.setUser(userVO);
        resultVO.setProfile(profileVO);
        resultVO.setSetting(settingVO);
        when(userProfileService.queryLoginUserProfile()).thenReturn(resultVO);

        mockMvc.perform(get("/api/user/profile/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.user.username").value("tester"))
                .andExpect(jsonPath("$.data.profile.gender").value(1))
                .andExpect(jsonPath("$.data.profile.country").value("中国"))
                .andExpect(jsonPath("$.data.profile.city").value("上海"))
                .andExpect(jsonPath("$.data.profile.githubUrl").value("https://github.com/tester"))
                .andExpect(jsonPath("$.data.profile.giteeUrl").value("https://gitee.com/tester"))
                .andExpect(jsonPath("$.data.profile.otherRepoLinks").value("https://gitlab.com/tester"))
                .andExpect(jsonPath("$.data.profile.blogUrl").value("https://blog.tester.com"))
                .andExpect(jsonPath("$.data.profile.signature").value("专注后端与架构"))
                .andExpect(jsonPath("$.data.profile.hobbyTags").value("阅读,跑步"))
                .andExpect(jsonPath("$.data.profile.techStack").value("Java,Spring Boot,MySQL"))
                .andExpect(jsonPath("$.data.profile.favoriteWebsites").value("https://spring.io,https://mybatis.plus"))
                .andExpect(jsonPath("$.data.profile.uploadedWebsites").value("https://demo.tester.com"))
                .andExpect(jsonPath("$.data.profile.contribution").value("256"))
                .andExpect(jsonPath("$.data.profile.deleted").value(0))
                .andExpect(jsonPath("$.data.setting.theme").value("dark"))
                .andExpect(jsonPath("$.data.setting.language").value("zh-CN"))
                .andExpect(jsonPath("$.data.setting.emailNotice").value(1))
                .andExpect(jsonPath("$.data.setting.collectNotice").value(1))
                .andExpect(jsonPath("$.data.setting.commentNotice").value(0))
                .andExpect(jsonPath("$.data.setting.homepageStyle").value("cards"))
                .andExpect(jsonPath("$.data.setting.pageSize").value(24));
        verify(userProfileService).queryLoginUserProfile();
    }

    /**
     * 上传头像应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldUploadAvatarSuccessfully() throws Exception {
        UserAvatarUploadVO uploadVO = new UserAvatarUploadVO();
        uploadVO.setAvatarUrl("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/new.png");
        uploadVO.setObjectKey("user-avatar/20260322/new.png");
        when(userProfileService.uploadLoginUserAvatar(any())).thenReturn(uploadVO);

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "avatar".getBytes());

        mockMvc.perform(multipart("/api/user/profile/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.avatarUrl").value("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/new.png"))
                .andExpect(jsonPath("$.data.objectKey").value("user-avatar/20260322/new.png"));
    }

    /**
     * 删除头像应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldDeleteAvatarSuccessfully() throws Exception {
        doNothing().when(userProfileService).deleteLoginUserAvatar();

        mockMvc.perform(delete("/api/user/profile/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS));

        verify(userProfileService).deleteLoginUserAvatar();
    }

    /**
     * 更新偏好设置成功应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldUpdateUserSettingSuccessfully() throws Exception {
        String requestBody = "{\"theme\":\"system\",\"language\":\"zh-CN\",\"emailNotice\":1,\"collectNotice\":1,\"commentNotice\":0,\"homepageStyle\":\"card\",\"pageSize\":20}";

        mockMvc.perform(put("/api/user/profile/setting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("偏好设置保存成功"));

        verify(userProfileService).updateLoginUserSetting(any());
    }

    /**
     * 修改用户名成功应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldUpdateUsernameSuccessfully() throws Exception {
        String requestBody = "{\"username\":\"new_username_2026\"}";

        mockMvc.perform(put("/api/user/profile/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("用户名修改成功"));

        verify(userProfileService).updateLoginUsername(any());
    }

    /**
     * 发送邮箱修改验证码应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldSendEmailUpdateCodeSuccessfully() throws Exception {
        String requestBody = "{\"email\":\"new_email@test.com\"}";

        mockMvc.perform(post("/api/user/profile/email/code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("验证码发送成功"));

        verify(userProfileService).sendEmailUpdateCode(any());
    }

    /**
     * 校验邮箱修改验证码应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldVerifyEmailUpdateCodeSuccessfully() throws Exception {
        String requestBody = "{\"email\":\"new_email@test.com\",\"emailCode\":\"123456\"}";

        mockMvc.perform(post("/api/user/profile/email/code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("验证码校验通过"));

        verify(userProfileService).verifyEmailUpdateCode(any());
    }

    /**
     * 修改邮箱成功应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldUpdateEmailSuccessfully() throws Exception {
        String requestBody = "{\"email\":\"new_email@test.com\",\"emailCode\":\"123456\"}";

        mockMvc.perform(put("/api/user/profile/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("邮箱修改成功"));

        verify(userProfileService).updateLoginEmail(any());
    }

    /**
     * 修改用户名失败应返回业务错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBusinessErrorWhenUpdateUsernameFailed() throws Exception {
        org.mockito.Mockito.doThrow(new BusinessException(
                        AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getCode(),
                        AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getMessage()))
                .when(userProfileService)
                .updateLoginUsername(any());

        String requestBody = "{\"username\":\"new_username_2026\"}";
        mockMvc.perform(put("/api/user/profile/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getMessage()));
    }

    /**
     * 未登录查询应返回未授权
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnUnauthorizedWhenNotLogin() throws Exception {
        when(userProfileService.queryLoginUserProfile())
                .thenThrow(new NotLoginException(NotLoginException.NOT_TOKEN, "login", "无token"));

        mockMvc.perform(get("/api/user/profile/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }
}
