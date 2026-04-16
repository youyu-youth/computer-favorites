package com.yyyouth.service.admin.report;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminReportBatchHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportQueryDTO;
import com.yyyouth.model.enums.ReportStatus;
import com.yyyouth.model.enums.ReportType;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.report.Report;
import com.yyyouth.model.pojo.system.AuditLog;
import com.yyyouth.model.pojo.website.Comment;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportDetailVO;
import com.yyyouth.model.vo.admin.AdminReportHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportPageVO;
import com.yyyouth.service.admin.report.impl.AdminReportServiceImpl;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.report.ReportMapper;
import com.yyyouth.service.mapper.system.AuditLogMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CommentMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    private static final long ADMIN_ID = 9001L;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private WebsiteMapper websiteMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private AdminAccountMapper adminAccountMapper;

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, Report.class);
        TableInfoHelper.initTableInfo(builderAssistant, Website.class);
        TableInfoHelper.initTableInfo(builderAssistant, Comment.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, AdminAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, AuditLog.class);
    }

    /**
     * 查询举报分页时应在关键字过滤后保持总数与记录口径一致
     */
    @Test
    void shouldQueryReportPageWithKeywordAgainstTargetAndReporterFields() {
        Report matchedReport = Report.builder()
                .id(101L)
                .userId(201L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(301L)
                .reason("内容误导：站点实际为付费工具")
                .images("[\"https://cdn.example.com/report-1.png\"]")
                .status(ReportStatus.PENDING.getCode())
                .handlerId(401L)
                .createTime(LocalDateTime.of(2026, 4, 15, 10, 0, 0))
                .updateTime(LocalDateTime.of(2026, 4, 15, 10, 5, 0))
                .build();
        Report unmatchedReport = Report.builder()
                .id(102L)
                .userId(202L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(302L)
                .reason("正常反馈")
                .status(ReportStatus.PENDING.getCode())
                .createTime(LocalDateTime.of(2026, 4, 15, 11, 0, 0))
                .updateTime(LocalDateTime.of(2026, 4, 15, 11, 5, 0))
                .build();

        UserAccount reporter = new UserAccount();
        reporter.setId(201L);
        reporter.setUsername("举报人甲");
        reporter.setEmail("reporter@example.com");

        UserAccount anotherReporter = new UserAccount();
        anotherReporter.setId(202L);
        anotherReporter.setUsername("普通用户");
        anotherReporter.setEmail("normal@example.com");

        Website website = new Website();
        website.setId(301L);
        website.setName("Digital Shortcut Hub");
        website.setUrl("https://example.com/tool");
        website.setSubmitterId(501L);

        Website anotherWebsite = new Website();
        anotherWebsite.setId(302L);
        anotherWebsite.setName("Knowledge Base");
        anotherWebsite.setUrl("https://example.com/wiki");
        anotherWebsite.setSubmitterId(502L);

        UserAccount uploader = new UserAccount();
        uploader.setId(501L);
        uploader.setUsername("发布者乙");

        UserAccount anotherUploader = new UserAccount();
        anotherUploader.setId(502L);
        anotherUploader.setUsername("发布者丁");

        AdminAccount handler = new AdminAccount();
        handler.setId(401L);
        handler.setUsername("管理员丙");

        when(reportMapper.selectList(any())).thenReturn(List.of(matchedReport, unmatchedReport));
        when(userAccountMapper.selectBatchIds(any())).thenReturn(List.of(reporter, anotherReporter, uploader, anotherUploader));
        when(websiteMapper.selectBatchIds(any())).thenReturn(List.of(website, anotherWebsite));
        when(adminAccountMapper.selectBatchIds(any())).thenReturn(List.of(handler));

        AdminReportQueryDTO queryDTO = new AdminReportQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setKeyword("shortcut");

        AdminReportPageVO result = adminReportService.queryReportPage(queryDTO);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getUserName()).isEqualTo("举报人甲");
        assertThat(result.getRecords().get(0).getUserEmail()).isEqualTo("reporter@example.com");
        assertThat(result.getRecords().get(0).getTargetName()).isEqualTo("Digital Shortcut Hub");
        assertThat(result.getRecords().get(0).getTargetUrl()).isEqualTo("https://example.com/tool");
        assertThat(result.getRecords().get(0).getUploaderName()).isEqualTo("发布者乙");
        assertThat(result.getRecords().get(0).getHandlerName()).isEqualTo("管理员丙");
        assertThat(result.getRecords().get(0).getImages()).containsExactly("https://cdn.example.com/report-1.png");
    }

    /**
     * 查询举报详情时应生成时间线与证据摘要
     */
    @Test
    void shouldQueryReportDetailWithTimeline() {
        Report report = Report.builder()
                .id(102L)
                .userId(202L)
                .type(ReportType.COMMENT.getCode())
                .websiteId(302L)
                .reason("评论区存在明显辱骂内容")
                .images("[\"https://cdn.example.com/report-2.png\",\"https://cdn.example.com/report-3.png\"]")
                .status(ReportStatus.PROCESSED.getCode())
                .handleResult("已确认违规并隐藏评论")
                .handlerId(402L)
                .handleTime(LocalDateTime.of(2026, 4, 16, 9, 30, 0))
                .createTime(LocalDateTime.of(2026, 4, 16, 8, 0, 0))
                .updateTime(LocalDateTime.of(2026, 4, 16, 9, 30, 0))
                .build();

        UserAccount reporter = new UserAccount();
        reporter.setId(202L);
        reporter.setUsername("夜巡用户");
        reporter.setEmail("night-watch@example.com");

        Comment comment = new Comment();
        comment.setId(302L);
        comment.setUserId(502L);
        comment.setContent("你这条评论毫无价值");
        comment.setStatus(0);

        UserAccount commentAuthor = new UserAccount();
        commentAuthor.setId(502L);
        commentAuthor.setUsername("评论作者");

        AdminAccount handler = new AdminAccount();
        handler.setId(402L);
        handler.setUsername("超级管理员");

        when(reportMapper.selectById(102L)).thenReturn(report);
        when(userAccountMapper.selectBatchIds(any())).thenReturn(List.of(reporter, commentAuthor));
        when(commentMapper.selectById(302L)).thenReturn(comment);
        when(adminAccountMapper.selectById(402L)).thenReturn(handler);

        AdminReportDetailVO result = adminReportService.queryReportDetail(102L);

        assertThat(result.getUserName()).isEqualTo("夜巡用户");
        assertThat(result.getTargetName()).contains("你这条评论毫无价值");
        assertThat(result.getTargetStatusLabel()).isEqualTo("评论已隐藏");
        assertThat(result.getEvidenceSummary()).contains("2 张截图证据");
        assertThat(result.getTimeline()).hasSize(2);
        assertThat(result.getTimeline().get(result.getTimeline().size() - 1).getTitle()).isEqualTo("举报已通过");
        assertThat(result.getHandlerName()).isEqualTo("超级管理员");
    }

    /**
     * 通过举报时应联动下架网站并返回处置结果
     */
    @Test
    void shouldHandleWebsiteReportAndOfflineTargetWhenPass() {
        Report pendingReport = Report.builder()
                .id(103L)
                .userId(203L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(303L)
                .status(ReportStatus.PENDING.getCode())
                .reason("资源违规")
                .createTime(LocalDateTime.of(2026, 4, 16, 8, 0, 0))
                .updateTime(LocalDateTime.of(2026, 4, 16, 8, 0, 0))
                .build();

        Website website = new Website();
        website.setId(303L);
        website.setStatus(1);

        when(reportMapper.selectById(103L)).thenReturn(pendingReport);
        when(reportMapper.update(any(Report.class), any())).thenReturn(1);
        when(websiteMapper.selectById(303L)).thenReturn(website);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        AdminReportHandleDTO handleDTO = new AdminReportHandleDTO();
        handleDTO.setAction("pass");
        handleDTO.setHandleResult("已确认违规内容，执行下架处理");
        handleDTO.setExecuteAction(Boolean.TRUE);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            AdminReportHandleResultVO result = adminReportService.handleReport(103L, handleDTO);

            assertThat(result.getReportId()).isEqualTo(103L);
            assertThat(result.getStatus()).isEqualTo(ReportStatus.PROCESSED.getCode());
            assertThat(result.getActionExecuted()).isTrue();
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(websiteCaptor.capture());
        assertThat(websiteCaptor.getValue().getStatus()).isEqualTo(0);
    }

    /**
     * 联动处置失败时应中断举报通过流程
     */
    @Test
    void shouldRejectPassHandleWhenGovernanceActionFails() {
        Report pendingReport = Report.builder()
                .id(108L)
                .userId(208L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(308L)
                .status(ReportStatus.PENDING.getCode())
                .reason("资源违规")
                .build();

        when(reportMapper.selectById(108L)).thenReturn(pendingReport);
        when(reportMapper.update(any(Report.class), any())).thenReturn(1);
        when(websiteMapper.selectById(308L)).thenReturn(null);

        AdminReportHandleDTO handleDTO = new AdminReportHandleDTO();
        handleDTO.setAction("pass");
        handleDTO.setHandleResult("确认违规，需要联动下架");
        handleDTO.setExecuteAction(Boolean.TRUE);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            assertThatThrownBy(() -> adminReportService.handleReport(108L, handleDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("目标联动处置失败");
        }
    }

    /**
     * 批量处置时应返回成功与失败明细
     */
    @Test
    void shouldBatchHandleReportsWithPartialFailure() {
        Report pendingReport = Report.builder()
                .id(104L)
                .userId(204L)
                .type(ReportType.COMMENT.getCode())
                .websiteId(304L)
                .status(ReportStatus.PENDING.getCode())
                .reason("广告灌水")
                .build();
        Report processedReport = Report.builder()
                .id(105L)
                .userId(205L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(305L)
                .status(ReportStatus.PROCESSED.getCode())
                .reason("已被处理")
                .build();

        when(reportMapper.selectById(104L)).thenReturn(pendingReport);
        when(reportMapper.selectById(105L)).thenReturn(processedReport);
        when(reportMapper.selectById(106L)).thenReturn(null);
        when(reportMapper.update(any(Report.class), any())).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class));
        });

        AdminReportBatchHandleDTO batchHandleDTO = new AdminReportBatchHandleDTO();
        batchHandleDTO.setReportIds(List.of(104L, 105L, 106L));
        batchHandleDTO.setAction("reject");
        batchHandleDTO.setHandleResult("批量核查后仅部分举报成立");
        batchHandleDTO.setExecuteAction(Boolean.FALSE);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            AdminReportBatchHandleResultVO result = adminReportService.batchHandleReports(batchHandleDTO);

            assertThat(result.getTotal()).isEqualTo(3);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isEqualTo(2);
            assertThat(result.getResults()).hasSize(3);
            assertThat(result.getResults().stream().filter(item -> !item.getSuccess())).hasSize(2);
        }
    }

    /**
     * 已处置举报不允许重复处理
     */
    @Test
    void shouldRejectHandleWhenReportAlreadyProcessed() {
        Report processedReport = Report.builder()
                .id(107L)
                .type(ReportType.WEBSITE.getCode())
                .websiteId(307L)
                .status(ReportStatus.PROCESSED.getCode())
                .build();
        when(reportMapper.selectById(107L)).thenReturn(processedReport);

        AdminReportHandleDTO handleDTO = new AdminReportHandleDTO();
        handleDTO.setAction("pass");
        handleDTO.setHandleResult("重复处置");
        handleDTO.setExecuteAction(Boolean.TRUE);

        assertThatThrownBy(() -> adminReportService.handleReport(107L, handleDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("举报已被处置");
    }
}
