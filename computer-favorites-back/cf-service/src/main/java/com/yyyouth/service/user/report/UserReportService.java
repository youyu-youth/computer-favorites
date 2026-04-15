package com.yyyouth.service.user.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.model.dto.user.ReportQueryDTO;
import com.yyyouth.model.dto.user.ReportSubmitDTO;
import com.yyyouth.model.vo.user.ReportDetailVO;
import com.yyyouth.model.vo.user.ReportListItemVO;
import com.yyyouth.model.vo.user.ReportSubmitVO;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 用户举报服务接口
 */
public interface UserReportService {

    /**
     * 提交举报
     *
     * @param submitDTO 举报提交参数
     * @param userId 当前用户ID
     * @return 举报提交结果
     */
    ReportSubmitVO submitReport(ReportSubmitDTO submitDTO, Long userId);

    /**
     * 我的举报列表
     *
     * @param queryDTO 查询参数
     * @param userId 当前用户ID
     * @return 举报列表分页数据
     */
    Page<ReportListItemVO> getMyReports(ReportQueryDTO queryDTO, Long userId);

    /**
     * 举报详情
     *
     * @param reportId 举报ID
     * @param userId 当前用户ID
     * @return 举报详情
     */
    ReportDetailVO getReportDetail(Long reportId, Long userId);
}
