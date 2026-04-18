package com.yyyouth.service.admin.feedback;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminFeedbackQueryDTO;
import com.yyyouth.model.dto.admin.AdminFeedbackReplyDTO;
import com.yyyouth.model.enums.FeedbackStatus;
import com.yyyouth.model.enums.FeedbackType;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.Feedback;
import com.yyyouth.model.vo.admin.AdminFeedbackDetailVO;
import com.yyyouth.model.vo.admin.AdminFeedbackHandleResultVO;
import com.yyyouth.model.vo.admin.AdminFeedbackPageVO;
import com.yyyouth.service.admin.feedback.impl.AdminFeedbackServiceImpl;
import com.yyyouth.service.mapper.system.FeedbackMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈处理服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminFeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @InjectMocks
    private AdminFeedbackServiceImpl adminFeedbackService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, Feedback.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
    }

    /**
     * 查询反馈分页时应返回统计摘要
     */
    @Test
    void shouldQueryFeedbackPageSuccessfully() {
        Feedback pendingFeedback = buildFeedback(9001L, 1001L, FeedbackType.BUG.getCode(), FeedbackStatus.PENDING.getCode());
        pendingFeedback.setImages("[\"https://cdn.example.com/feedback/1.png\"]");
        pendingFeedback.setContact("tester@example.com");

        Feedback processedFeedback = buildFeedback(9002L, 1002L, FeedbackType.SUGGESTION.getCode(), FeedbackStatus.PROCESSED.getCode());
        when(feedbackMapper.selectList(any())).thenReturn(List.of(pendingFeedback, processedFeedback));
        when(userAccountMapper.selectBatchIds(any())).thenReturn(List.of(
                buildUser(1001L, "张三", "zhangsan@example.com"),
                buildUser(1002L, "李四", "lisi@example.com")
        ));

        AdminFeedbackQueryDTO queryDTO = new AdminFeedbackQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        AdminFeedbackPageVO pageVO = adminFeedbackService.queryFeedbackPage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(2);
        assertThat(pageVO.getRecords()).hasSize(2);
        assertThat(pageVO.getStatistics().getPending()).isEqualTo(1);
        assertThat(pageVO.getStatistics().getProcessed()).isEqualTo(1);
        assertThat(pageVO.getStatistics().getBugCount()).isEqualTo(1);
        assertThat(pageVO.getStatistics().getWithImagesCount()).isEqualTo(1);
        assertThat(pageVO.getStatistics().getWithContactCount()).isEqualTo(1);
    }

    /**
     * 查询反馈详情时应返回时间线
     */
    @Test
    void shouldQueryFeedbackDetailSuccessfully() {
        Feedback feedback = buildFeedback(9003L, 1003L, FeedbackType.BUG.getCode(), FeedbackStatus.PROCESSED.getCode());
        feedback.setReply("问题已确认，修复计划已排期。");
        feedback.setReplyTime(LocalDateTime.now().minusHours(1));
        when(feedbackMapper.selectById(9003L)).thenReturn(feedback);
        when(userAccountMapper.selectById(1003L)).thenReturn(buildUser(1003L, "王五", "wangwu@example.com"));

        AdminFeedbackDetailVO detailVO = adminFeedbackService.queryFeedbackDetail(9003L);

        assertThat(detailVO.getId()).isEqualTo(9003L);
        assertThat(detailVO.getReply()).isEqualTo("问题已确认，修复计划已排期。");
        assertThat(detailVO.getTimeline()).hasSize(2);
        assertThat(detailVO.getSummaryText()).contains("已完成回复");
    }

    /**
     * 回复反馈成功时应返回处理结果
     */
    @Test
    void shouldReplyFeedbackSuccessfully() {
        Feedback feedback = buildFeedback(9004L, 1004L, FeedbackType.BUG.getCode(), FeedbackStatus.PENDING.getCode());
        when(feedbackMapper.selectById(9004L)).thenReturn(feedback);
        when(feedbackMapper.update(any(Feedback.class), any())).thenReturn(1);

        AdminFeedbackReplyDTO replyDTO = new AdminFeedbackReplyDTO();
        replyDTO.setReply("这里是符合长度要求的反馈回复内容。");

        AdminFeedbackHandleResultVO resultVO = adminFeedbackService.replyFeedback(9004L, replyDTO);

        assertThat(resultVO.getFeedbackId()).isEqualTo(9004L);
        assertThat(resultVO.getStatus()).isEqualTo(FeedbackStatus.PROCESSED.getCode());
        assertThat(resultVO.getReply()).isEqualTo("这里是符合长度要求的反馈回复内容。");
        assertThat(resultVO.getReplyTime()).isNotNull();
    }

    /**
     * 已关闭反馈再次回复时应抛出业务异常
     */
    @Test
    void shouldThrowWhenReplyClosedFeedback() {
        Feedback feedback = buildFeedback(9005L, 1005L, FeedbackType.BUG.getCode(), FeedbackStatus.CLOSED.getCode());
        when(feedbackMapper.selectById(9005L)).thenReturn(feedback);

        AdminFeedbackReplyDTO replyDTO = new AdminFeedbackReplyDTO();
        replyDTO.setReply("这里是符合长度要求的反馈回复内容。");

        assertThatThrownBy(() -> adminFeedbackService.replyFeedback(9005L, replyDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已关闭反馈不支持继续回复");
    }

    /**
     * 重复关闭反馈时应抛出业务异常
     */
    @Test
    void shouldThrowWhenCloseClosedFeedback() {
        Feedback feedback = buildFeedback(9006L, 1006L, FeedbackType.COMPLAINT.getCode(), FeedbackStatus.CLOSED.getCode());
        when(feedbackMapper.selectById(9006L)).thenReturn(feedback);

        assertThatThrownBy(() -> adminFeedbackService.closeFeedback(9006L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该反馈已关闭，请勿重复操作");
    }

    /**
     * 构建反馈实体
     *
     * @param id 主键
     * @param userId 用户ID
     * @param type 类型
     * @param status 状态
     * @return 反馈实体
     */
    private Feedback buildFeedback(Long id, Long userId, Integer type, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return Feedback.builder()
                .id(id)
                .userId(userId)
                .avatar("https://cdn.example.com/avatar.png")
                .type(type)
                .content("这里是一条足够长的反馈内容，用于测试管理端反馈处理逻辑。")
                .status(status)
                .createTime(now.minusHours(2))
                .updateTime(now.minusHours(1))
                .build();
    }

    /**
     * 构建用户实体
     *
     * @param id 主键
     * @param username 用户名
     * @param email 邮箱
     * @return 用户实体
     */
    private UserAccount buildUser(Long id, String username, String email) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(id);
        userAccount.setUsername(username);
        userAccount.setEmail(email);
        userAccount.setAvatar("https://cdn.example.com/avatar.png");
        return userAccount;
    }
}
