package com.yyyouth.service.user;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.CaptchaConstants;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.EmailUtils;
import com.yyyouth.model.dto.user.UserEmailCodeSendDTO;
import com.yyyouth.model.dto.user.UserEmailUpdateDTO;
import com.yyyouth.model.dto.user.UserPreferenceSettingUpdateDTO;
import com.yyyouth.model.dto.user.UserUsernameUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.TechStack;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.user.TechStackMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.user.profile.impl.UserProfileServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户资料服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserSettingMapper userSettingMapper;

    @Mock
    private TechStackMapper techStackMapper;

    @Mock
    private MinioFileService minioFileService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private EmailUtils emailUtils;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, TechStack.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserProfile.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserSetting.class);
    }

    /**
     * 查询当前用户资料时应按最新字段映射并移除设置表 deleted 条件
     */
    @Test
    void shouldQueryCurrentProfileWithoutUserSettingDeletedCondition() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1001L);
        userAccount.setUsername("tester");
        userAccount.setNickname("测试用户");
        userAccount.setDeleted(0);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(1001L);
        userProfile.setGender(1);
        userProfile.setCountry("中国");
        userProfile.setCity("上海");
        userProfile.setGithubUrl("https://github.com/tester");
        userProfile.setGiteeUrl("https://gitee.com/tester");
        userProfile.setOtherRepoLinks("https://gitlab.com/tester");
        userProfile.setBlogUrl("https://blog.tester.com");
        userProfile.setSignature("专注后端与架构");
        userProfile.setHobbyTags("阅读,跑步");
        userProfile.setTechStack("1,2,3");
        userProfile.setFavoriteWebsites("https://spring.io,https://mybatis.plus");
        userProfile.setUploadedWebsites("https://demo.tester.com");
        userProfile.setContribution("256");
        userProfile.setDeleted(0);
        userProfile.setCreateTime(LocalDateTime.of(2026, 3, 18, 10, 0));
        userProfile.setUpdateTime(LocalDateTime.of(2026, 3, 18, 11, 0));

        UserSetting userSetting = new UserSetting();
        userSetting.setUserId(1001L);
        userSetting.setTheme("dark");
        userSetting.setLanguage("zh-CN");
        userSetting.setEmailNotice(1);
        userSetting.setCollectNotice(1);
        userSetting.setCommentNotice(0);
        userSetting.setHomepageStyle("cards");
        userSetting.setPageSize(24);

        TechStack javaStack = new TechStack();
        javaStack.setId(1L);
        javaStack.setName("Java");
        TechStack springBootStack = new TechStack();
        springBootStack.setId(2L);
        springBootStack.setName("Spring Boot");
        TechStack mysqlStack = new TechStack();
        mysqlStack.setId(3L);
        mysqlStack.setName("MySQL");

        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(userProfileMapper.selectOne(any())).thenReturn(userProfile);
        when(userSettingMapper.selectOne(any())).thenReturn(userSetting);
        when(techStackMapper.selectList(any())).thenReturn(List.of(javaStack, springBootStack, mysqlStack));

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            LoginUserProfileVO resultVO = userProfileService.queryLoginUserProfile();

            assertThat(resultVO.getUser()).isNotNull();
            assertThat(resultVO.getUser().getUsername()).isEqualTo("tester");
            assertThat(resultVO.getProfile()).isNotNull();
            assertThat(resultVO.getProfile().getGender()).isEqualTo(1);
            assertThat(resultVO.getProfile().getCountry()).isEqualTo("中国");
            assertThat(resultVO.getProfile().getCity()).isEqualTo("上海");
            assertThat(resultVO.getProfile().getGithubUrl()).isEqualTo("https://github.com/tester");
            assertThat(resultVO.getProfile().getGiteeUrl()).isEqualTo("https://gitee.com/tester");
            assertThat(resultVO.getProfile().getOtherRepoLinks()).isEqualTo("https://gitlab.com/tester");
            assertThat(resultVO.getProfile().getBlogUrl()).isEqualTo("https://blog.tester.com");
            assertThat(resultVO.getProfile().getSignature()).isEqualTo("专注后端与架构");
            assertThat(resultVO.getProfile().getHobbyTags()).isEqualTo("阅读,跑步");
            assertThat(resultVO.getProfile().getTechStack()).isEqualTo("Java,Spring Boot,MySQL");
            assertThat(resultVO.getProfile().getFavoriteWebsites()).isEqualTo("https://spring.io,https://mybatis.plus");
            assertThat(resultVO.getProfile().getUploadedWebsites()).isEqualTo("https://demo.tester.com");
            assertThat(resultVO.getProfile().getContribution()).isEqualTo("256");
            assertThat(resultVO.getProfile().getDeleted()).isEqualTo(0);
            assertThat(resultVO.getProfile().getCreateTime()).isEqualTo(LocalDateTime.of(2026, 3, 18, 10, 0));
            assertThat(resultVO.getProfile().getUpdateTime()).isEqualTo(LocalDateTime.of(2026, 3, 18, 11, 0));
            assertThat(resultVO.getSetting()).isNotNull();
            assertThat(resultVO.getSetting().getTheme()).isEqualTo("dark");
            assertThat(resultVO.getSetting().getLanguage()).isEqualTo("zh-CN");
            assertThat(resultVO.getSetting().getEmailNotice()).isEqualTo(1);
            assertThat(resultVO.getSetting().getCollectNotice()).isEqualTo(1);
            assertThat(resultVO.getSetting().getCommentNotice()).isEqualTo(0);
            assertThat(resultVO.getSetting().getHomepageStyle()).isEqualTo("cards");
            assertThat(resultVO.getSetting().getPageSize()).isEqualTo(24);
        }

        verify(userSettingMapper).selectOne(argThat((Wrapper<UserSetting> wrapper) ->
                !wrapper.getSqlSegment().toLowerCase().contains("deleted")));
    }

    /**
     * 上传头像时应更新用户头像并删除旧头像对象
     */
    @Test
    void shouldUploadAvatarAndDeleteOldAvatar() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1001L);
        userAccount.setDeleted(0);
        userAccount.setAvatar("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/old.png");

        UserAvatarUploadVO uploadVO = new UserAvatarUploadVO();
        uploadVO.setAvatarUrl("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/new.png");
        uploadVO.setObjectKey("user-avatar/20260322/new.png");

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(minioFileService.uploadAvatar(file, "user-avatar")).thenReturn(uploadVO);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            UserAvatarUploadVO result = userProfileService.uploadLoginUserAvatar(file);
            assertThat(result.getAvatarUrl()).isEqualTo(uploadVO.getAvatarUrl());
        }

        verify(userAccountMapper).updateById(argThat((UserAccount account) ->
                "http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/new.png".equals(account.getAvatar())));
        verify(minioFileService).deleteByUrl("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/old.png");
    }

    /**
     * 删除头像时应清空用户头像字段
     */
    @Test
    void shouldDeleteAvatarAndClearUserAvatarField() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1001L);
        userAccount.setDeleted(0);
        userAccount.setAvatar("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/old.png");

        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            userProfileService.deleteLoginUserAvatar();
        }

        verify(minioFileService).deleteByUrl("http://127.0.0.1:9000/computer-favorites/user-avatar/20260322/old.png");
        verify(userAccountMapper).updateById(argThat((UserAccount account) -> "".equals(account.getAvatar())));
    }

    /**
     * 修改用户名成功时应更新用户名与修改时间
     */
    @Test
    void shouldUpdateUsernameSuccessfully() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);
        currentUser.setUsername("old_name");
        currentUser.setUsernameUpdateTime(java.time.LocalDateTime.of(2026, 2, 15, 10, 0));

        UserUsernameUpdateDTO updateDTO = new UserUsernameUpdateDTO();
        updateDTO.setUsername("new_name_2026");

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser).thenReturn(null);
        when(userAccountMapper.update(any(), any())).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
            userProfileService.updateLoginUsername(updateDTO);
        }

        verify(userAccountMapper).update(any(), any());
    }

    /**
     * 同一自然月重复修改用户名时应拒绝
     */
    @Test
    void shouldRejectUsernameUpdateWhenSameMonth() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);
        currentUser.setUsername("old_name");
        currentUser.setUsernameUpdateTime(java.time.LocalDateTime.now().minusDays(1));

        UserUsernameUpdateDTO updateDTO = new UserUsernameUpdateDTO();
        updateDTO.setUsername("new_name_2026");

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userProfileService.updateLoginUsername(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getMessage());
        }
    }

    /**
     * 发送邮箱修改验证码成功时应写入验证码缓存
     */
    @Test
    void shouldSendEmailUpdateCodeSuccessfully() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);
        currentUser.setEmail("old@test.com");

        UserEmailCodeSendDTO sendDTO = new UserEmailCodeSendDTO();
        sendDTO.setEmail("new@test.com");

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = org.mockito.Mockito.mock(ValueOperations.class);

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser).thenReturn(null);
        when(stringRedisTemplate.hasKey(any())).thenReturn(false);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(emailUtils.sendGeneralEmail(any(), any(), any())).thenReturn(true);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
            userProfileService.sendEmailUpdateCode(sendDTO);
        }

        verify(valueOperations).set(
                any(),
                any(),
                org.mockito.ArgumentMatchers.eq((long) CaptchaConstants.CODE_EXPIRE_MINUTES),
                org.mockito.ArgumentMatchers.eq(java.util.concurrent.TimeUnit.MINUTES)
        );
    }

    /**
     * 修改邮箱成功时应更新邮箱并清理验证码缓存
     */
    @Test
    void shouldUpdateLoginEmailSuccessfully() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);
        currentUser.setEmail("old@test.com");

        UserEmailUpdateDTO updateDTO = new UserEmailUpdateDTO();
        updateDTO.setEmail("new@test.com");
        updateDTO.setEmailCode("123456");

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = org.mockito.Mockito.mock(ValueOperations.class);

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("123456");
        when(userAccountMapper.update(any(), any())).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
            userProfileService.updateLoginEmail(updateDTO);
        }

        verify(userAccountMapper).update(any(), any());
        verify(stringRedisTemplate).delete(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * 修改邮箱验证码错误时应拒绝更新
     */
    @Test
    void shouldRejectUpdateEmailWhenCodeInvalid() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);
        currentUser.setEmail("old@test.com");

        UserEmailUpdateDTO updateDTO = new UserEmailUpdateDTO();
        updateDTO.setEmail("new@test.com");
        updateDTO.setEmailCode("654321");

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = org.mockito.Mockito.mock(ValueOperations.class);

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("123456");

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userProfileService.updateLoginEmail(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorCode.EMAIL_UPDATE_CODE_INVALID.getMessage());
        }

        verify(userAccountMapper, never()).update(any(), any());
    }

    /**
     * 更新偏好设置无记录时应新增
     */
    @Test
    void shouldInsertUserSettingWhenSettingNotExists() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);

        UserPreferenceSettingUpdateDTO updateDTO = new UserPreferenceSettingUpdateDTO();
        updateDTO.setTheme("system");
        updateDTO.setLanguage("zh-CN");
        updateDTO.setEmailNotice(1);
        updateDTO.setCollectNotice(1);
        updateDTO.setCommentNotice(0);
        updateDTO.setHomepageStyle("card");
        updateDTO.setPageSize(20);

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);
        when(userSettingMapper.selectOne(any())).thenReturn(null);
        when(userSettingMapper.insert(any(UserSetting.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);
            userProfileService.updateLoginUserSetting(updateDTO);
        }

        verify(userSettingMapper).insert(argThat((UserSetting setting) ->
                "system".equals(setting.getTheme())
                        && "zh-CN".equals(setting.getLanguage())
                        && Integer.valueOf(20).equals(setting.getPageSize())));
    }

    /**
     * 更新偏好设置参数非法时应拒绝
     */
    @Test
    void shouldRejectUpdateUserSettingWhenThemeInvalid() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(1001L);
        currentUser.setDeleted(0);

        UserPreferenceSettingUpdateDTO updateDTO = new UserPreferenceSettingUpdateDTO();
        updateDTO.setTheme("blue");
        updateDTO.setLanguage("zh-CN");
        updateDTO.setEmailNotice(1);
        updateDTO.setCollectNotice(1);
        updateDTO.setCommentNotice(1);
        updateDTO.setHomepageStyle("card");
        updateDTO.setPageSize(20);

        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);
        when(userSettingMapper.selectOne(any())).thenReturn(new UserSetting());

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userProfileService.updateLoginUserSetting(updateDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorCode.USER_SETTING_INVALID_PARAM.getMessage());
        }
    }

    /**
     * 查询资料时用户禁用应拒绝
     */
    @Test
    void shouldRejectQueryProfileWhenUserDisabled() {
        when(userAccountMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userProfileService.queryLoginUserProfile())
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorCode.USER_DISABLED.getMessage());
        }
    }

    /**
     * 更新资料时用户禁用应拒绝
     */
    @Test
    void shouldRejectUpdateProfileWhenUserDisabled() {
        when(userAccountMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userProfileService.updateLoginUserProfile(new com.yyyouth.model.dto.user.UserProfileUpdateDTO()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorCode.USER_DISABLED.getMessage());
        }
    }
}
