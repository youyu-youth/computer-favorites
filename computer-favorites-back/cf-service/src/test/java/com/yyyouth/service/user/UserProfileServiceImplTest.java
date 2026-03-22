package com.yyyouth.service.user;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.TechStack;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.user.TechStackMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.user.impl.UserProfileServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
}
