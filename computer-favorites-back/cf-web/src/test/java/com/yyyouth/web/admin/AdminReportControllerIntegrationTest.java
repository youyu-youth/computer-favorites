package com.yyyouth.web.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.model.dto.admin.AdminReportBatchHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportHandleDTO;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultItemVO;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportDetailTimelineItemVO;
import com.yyyouth.model.vo.admin.AdminReportDetailVO;
import com.yyyouth.model.vo.admin.AdminReportHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportListItemVO;
import com.yyyouth.model.vo.admin.AdminReportPageVO;
import com.yyyouth.model.vo.admin.AdminReportStatisticsVO;
import com.yyyouth.service.admin.report.AdminReportService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.admin.AdminReportController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报控制器集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminReportControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    @Mock
    private AdminReportService adminReportService;

    @InjectMocks
    private AdminReportController adminReportController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminReportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询举报列表成功应返回分页数据
     */
    @Test
    void shouldReturnReportPageWhenList() throws Exception {
        AdminReportListItemVO itemVO = new AdminReportListItemVO();
        itemVO.setId(101L);
        itemVO.setUserName("举报人甲");
        itemVO.setUserEmail("reporter@example.com");
        itemVO.setTargetName("Digital Shortcut Hub");
        itemVO.setImages(List.of("https://cdn.example.com/1.png"));

        AdminReportPageVO pageVO = new AdminReportPageVO();
        pageVO.setRecords(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(6);
        pageVO.setTotalPages(1L);

        when(adminReportService.queryReportPage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/admin/report/list")
                        .param("pageNum", "1")
                        .param("pageSize", "6")
                        .param("keyword", "shortcut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].userEmail").value("reporter@example.com"));

        verify(adminReportService).queryReportPage(any());
    }

    /**
     * 查询举报详情成功应返回详情数据
     */
    @Test
    void shouldReturnReportDetailWhenDetail() throws Exception {
        AdminReportDetailTimelineItemVO timelineItemVO = new AdminReportDetailTimelineItemVO();
        timelineItemVO.setId("handled-101");
        timelineItemVO.setTitle("举报已通过");
        timelineItemVO.setDescription("已确认违规并完成处置");
        timelineItemVO.setTime("2026-04-16 10:00:00");
        timelineItemVO.setTone("done");

        AdminReportDetailVO detailVO = new AdminReportDetailVO();
        detailVO.setId(101L);
        detailVO.setUserName("举报人甲");
        detailVO.setTargetName("评论 #2001");
        detailVO.setTargetStatusLabel("评论已隐藏");
        detailVO.setEvidenceSummary("已提交 1 张截图证据，可用于辅助判定。");
        detailVO.setTimeline(List.of(timelineItemVO));

        when(adminReportService.queryReportDetail(101L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/admin/report/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.targetStatusLabel").value("评论已隐藏"))
                .andExpect(jsonPath("$.data.timeline[0].title").value("举报已通过"));

        verify(adminReportService).queryReportDetail(101L);
    }

    /**
     * 单条处置成功应返回处置结果
     */
    @Test
    void shouldReturnHandleResultWhenHandleReport() throws Exception {
        AdminReportHandleDTO handleDTO = new AdminReportHandleDTO();
        handleDTO.setAction("pass");
        handleDTO.setHandleResult("已确认违规内容，执行下架处理");
        handleDTO.setExecuteAction(Boolean.TRUE);

        AdminReportHandleResultVO resultVO = new AdminReportHandleResultVO();
        resultVO.setReportId(101L);
        resultVO.setStatus(1);
        resultVO.setHandleResult("已确认违规内容，执行下架处理");
        resultVO.setHandleTime(LocalDateTime.of(2026, 4, 16, 10, 0, 0));
        resultVO.setActionExecuted(Boolean.TRUE);

        when(adminReportService.handleReport(eq(101L), any())).thenReturn(resultVO);

        mockMvc.perform(put("/api/admin/report/101/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(handleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("处置成功"))
                .andExpect(jsonPath("$.data.actionExecuted").value(true));

        verify(adminReportService).handleReport(eq(101L), any());
    }

    /**
     * 批量处置成功应返回统计结果
     */
    @Test
    void shouldReturnBatchHandleResultWhenBatchHandle() throws Exception {
        AdminReportBatchHandleDTO batchHandleDTO = new AdminReportBatchHandleDTO();
        batchHandleDTO.setReportIds(List.of(101L, 102L));
        batchHandleDTO.setAction("reject");
        batchHandleDTO.setHandleResult("批量核查后处理已经完成");
        batchHandleDTO.setExecuteAction(Boolean.FALSE);

        AdminReportBatchHandleResultItemVO itemVO = new AdminReportBatchHandleResultItemVO();
        itemVO.setReportId(101L);
        itemVO.setSuccess(Boolean.TRUE);
        itemVO.setMessage("处置成功");

        AdminReportBatchHandleResultVO resultVO = new AdminReportBatchHandleResultVO();
        resultVO.setTotal(2);
        resultVO.setSuccess(1);
        resultVO.setFailed(1);
        resultVO.setResults(List.of(itemVO));

        when(adminReportService.batchHandleReports(any())).thenReturn(resultVO);

        mockMvc.perform(put("/api/admin/report/handle/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchHandleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("批量处置完成"))
                .andExpect(jsonPath("$.data.failed").value(1));

        verify(adminReportService).batchHandleReports(any());
    }

    /**
     * 查询统计成功应返回看板数据
     */
    @Test
    void shouldReturnStatisticsWhenQueryStatistics() throws Exception {
        AdminReportStatisticsVO statisticsVO = new AdminReportStatisticsVO();
        statisticsVO.setTotal(12);
        statisticsVO.setPending(3);
        statisticsVO.setProcessed(7);
        statisticsVO.setRejected(2);
        statisticsVO.setLast24Hours(4);
        statisticsVO.setWebsiteCount(8);
        statisticsVO.setCommentCount(4);
        statisticsVO.setProcessRate(75);

        when(adminReportService.queryReportStatistics(null, null)).thenReturn(statisticsVO);

        mockMvc.perform(get("/api/admin/report/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.processRate").value(75))
                .andExpect(jsonPath("$.data.pending").value(3));

        verify(adminReportService).queryReportStatistics(null, null);
    }
}
