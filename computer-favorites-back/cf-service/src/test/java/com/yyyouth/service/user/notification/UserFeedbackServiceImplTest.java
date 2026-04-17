package com.yyyouth.service.user.notification;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserFeedbackCreateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.Feedback;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.model.vo.user.UserFeedbackImageUploadVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.system.FeedbackMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.user.notification.impl.UserFeedbackServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserFeedbackServiceImplTest {

    private static final Long LOGIN_USER_ID = 1001L;

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private MinioFileService minioFileService;

    @InjectMocks
    private UserFeedbackServiceImpl userFeedbackService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, Feedback.class);
    }

    /**
     * 反馈提交成功时应写入用户信息和图片 JSON
     */
    @Test
    void shouldSubmitFeedbackSuccessfully() {
        UserFeedbackCreateDTO createDTO = new UserFeedbackCreateDTO();
        createDTO.setType(2);
        createDTO.setContent("这里有一个稳定复现的交互问题，麻烦排查。");
        createDTO.setContact("tester@example.com");
        createDTO.setImages(List.of(
                "https://cdn.example.com/feedback/1.png",
                "https://cdn.example.com/feedback/2.png"
        ));

        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setAvatar("https://cdn.example.com/avatar/tester.png");
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);
        when(feedbackMapper.insert(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback feedback = invocation.getArgument(0);
            feedback.setId(9001L);
            return 1;
        });

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userFeedbackService.submitFeedback(createDTO);
        }

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(feedbackCaptor.capture());

        Feedback insertedFeedback = feedbackCaptor.getValue();
        assertThat(insertedFeedback.getUserId()).isEqualTo(LOGIN_USER_ID);
        assertThat(insertedFeedback.getAvatar()).isEqualTo("https://cdn.example.com/avatar/tester.png");
        assertThat(insertedFeedback.getStatus()).isEqualTo(0);
        assertThat(insertedFeedback.getImages()).isEqualTo(
                "[\"https://cdn.example.com/feedback/1.png\",\"https://cdn.example.com/feedback/2.png\"]"
        );
    }

    /**
     * 用户无头像时应使用空字符串落库，避免违反非空约束
     */
    @Test
    void shouldSubmitFeedbackWithEmptyAvatarSuccessfully() {
        UserFeedbackCreateDTO createDTO = new UserFeedbackCreateDTO();
        createDTO.setType(1);
        createDTO.setContent("希望首页分类筛选可以支持记忆上次选择。");

        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setAvatar("   ");
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);
        when(feedbackMapper.insert(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback feedback = invocation.getArgument(0);
            feedback.setId(9002L);
            return 1;
        });

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userFeedbackService.submitFeedback(createDTO);
        }

        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(feedbackCaptor.capture());
        assertThat(feedbackCaptor.getValue().getAvatar()).isEmpty();
    }

    /**
     * 图片地址不是正式 URL 时应拒绝提交
     */
    @Test
    void shouldThrowWhenSubmitFeedbackWithInvalidImageUrl() {
        UserFeedbackCreateDTO createDTO = new UserFeedbackCreateDTO();
        createDTO.setType(1);
        createDTO.setContent("这个功能建议可以进一步优化引导提示。");
        createDTO.setImages(List.of("blob:http://localhost/mock"));

        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userFeedbackService.submitFeedback(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("图片地址不合法，请重新上传");
        }
    }

    /**
     * 内容命中敏感词时应拒绝提交
     */
    @Test
    void shouldThrowWhenSubmitFeedbackContainsSensitiveWord() {
        UserFeedbackCreateDTO createDTO = new UserFeedbackCreateDTO();
        createDTO.setType(3);
        createDTO.setContent("这里包含法轮功等敏感词内容用于测试。");

        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userFeedbackService.submitFeedback(createDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("反馈内容包含敏感词");
        }
    }

    /**
     * 上传反馈图片成功时应返回对象键与访问地址
     */
    @Test
    void shouldUploadFeedbackImageSuccessfully() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);

        MinioUploadVO uploadVO = new MinioUploadVO();
        uploadVO.setObjectKey("user/feedback/image/202604/test.png");
        uploadVO.setFileUrl("https://minio.local/user/feedback/image/202604/test.png");
        when(minioFileService.uploadImageByMonth(file, "user", "feedback", "image", 2 * 1024 * 1024L))
                .thenReturn(uploadVO);

        UserFeedbackImageUploadVO result;
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            result = userFeedbackService.uploadFeedbackImage(file);
        }

        assertThat(result.getObjectKey()).isEqualTo("user/feedback/image/202604/test.png");
        assertThat(result.getImageUrl()).isEqualTo("https://minio.local/user/feedback/image/202604/test.png");
    }

    /**
     * 删除图片对象键前缀不合法时应抛出业务异常
     */
    @Test
    void shouldThrowWhenDeleteFeedbackImageWithInvalidObjectKey() {
        UserAccount currentUser = new UserAccount();
        currentUser.setId(LOGIN_USER_ID);
        currentUser.setDeleted(0);
        currentUser.setStatus(1);
        when(userAccountMapper.selectOne(any())).thenReturn(currentUser);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userFeedbackService.deleteFeedbackImage("user/profile/avatar/test.png"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("反馈图片对象键不合法");
        }
    }
}
