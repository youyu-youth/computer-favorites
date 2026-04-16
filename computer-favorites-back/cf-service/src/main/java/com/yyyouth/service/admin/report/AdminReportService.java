package com.yyyouth.service.admin.report;

import com.yyyouth.model.dto.admin.AdminReportBatchHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportQueryDTO;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportDetailVO;
import com.yyyouth.model.vo.admin.AdminReportHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportPageVO;
import com.yyyouth.model.vo.admin.AdminReportStatisticsVO;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报处置服务
 */
public interface AdminReportService {

    /**
     * 查询举报分页
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    AdminReportPageVO queryReportPage(AdminReportQueryDTO queryDTO);

    /**
     * 查询举报详情
     *
     * @param reportId 举报ID
     * @return 举报详情
     */
    AdminReportDetailVO queryReportDetail(Long reportId);

    /**
     * 单条处置举报
     *
     * @param reportId 举报ID
     * @param handleDTO 处置参数
     * @return 处置结果
     */
    AdminReportHandleResultVO handleReport(Long reportId, AdminReportHandleDTO handleDTO);

    /**
     * 批量处置举报
     *
     * @param batchHandleDTO 批量处置参数
     * @return 批量处置结果
     */
    AdminReportBatchHandleResultVO batchHandleReports(AdminReportBatchHandleDTO batchHandleDTO);

    /**
     * 查询举报统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    AdminReportStatisticsVO queryReportStatistics(String startTime, String endTime);
}
