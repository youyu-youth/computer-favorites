package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminReportBatchHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportHandleDTO;
import com.yyyouth.model.dto.admin.AdminReportQueryDTO;
import com.yyyouth.model.vo.admin.AdminReportBatchHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportDetailVO;
import com.yyyouth.model.vo.admin.AdminReportHandleResultVO;
import com.yyyouth.model.vo.admin.AdminReportPageVO;
import com.yyyouth.model.vo.admin.AdminReportStatisticsVO;
import com.yyyouth.service.admin.report.AdminReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报处置控制器
 */
@Slf4j
@Api(tags = "管理端举报处置接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/report")
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 查询举报分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询举报分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:report:list", type = "admin")
    public HttpResult list(@Valid AdminReportQueryDTO queryDTO) {
        log.info("查询管理端举报列表请求，pageNum={}, pageSize={}, status={}, type={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getStatus(), queryDTO.getType());
        AdminReportPageVO pageVO = adminReportService.queryReportPage(queryDTO);
        log.info("查询管理端举报列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    @ApiOperation(value = "查询举报详情")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "admin:report:detail", type = "admin")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("查询管理端举报详情请求，id={}", id);
        AdminReportDetailVO detailVO = adminReportService.queryReportDetail(id);
        log.info("查询管理端举报详情成功，id={}", id);
        return HttpResult.success("查询成功", detailVO);
    }

    /**
     * 单条处置举报
     *
     * @param id 举报ID
     * @param handleDTO 处置参数
     * @return 处置结果
     */
    @ApiOperation(value = "单条处置举报")
    @PutMapping("/{id}/handle")
    @SaCheckPermission(value = "admin:report:handle", type = "admin")
    public HttpResult handle(@PathVariable("id") @NotNull @Positive Long id,
                             @RequestBody @Valid @NotNull AdminReportHandleDTO handleDTO) {
        log.info("管理端处置举报请求，id={}, action={}, executeAction={}", id, handleDTO.getAction(), handleDTO.getExecuteAction());
        AdminReportHandleResultVO resultVO = adminReportService.handleReport(id, handleDTO);
        log.info("管理端处置举报成功，id={}, status={}", id, resultVO.getStatus());
        return HttpResult.success("处置成功", resultVO);
    }

    /**
     * 批量处置举报
     *
     * @param batchHandleDTO 批量处置参数
     * @return 批量处置结果
     */
    @ApiOperation(value = "批量处置举报")
    @PutMapping("/handle/batch")
    @SaCheckPermission(value = "admin:report:handle", type = "admin")
    public HttpResult batchHandle(@RequestBody @Valid @NotNull AdminReportBatchHandleDTO batchHandleDTO) {
        log.info("管理端批量处置举报请求，count={}, action={}", batchHandleDTO.getReportIds().size(), batchHandleDTO.getAction());
        AdminReportBatchHandleResultVO resultVO = adminReportService.batchHandleReports(batchHandleDTO);
        log.info("管理端批量处置举报完成，success={}, failed={}", resultVO.getSuccess(), resultVO.getFailed());
        return HttpResult.success("批量处置完成", resultVO);
    }

    /**
     * 查询举报统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @ApiOperation(value = "查询举报统计")
    @GetMapping("/statistics")
    @SaCheckPermission(value = "admin:report:statistics", type = "admin")
    public HttpResult statistics(@RequestParam(value = "startTime", required = false) String startTime,
                                 @RequestParam(value = "endTime", required = false) String endTime) {
        log.info("查询管理端举报统计请求，startTime={}, endTime={}", startTime, endTime);
        AdminReportStatisticsVO statisticsVO = adminReportService.queryReportStatistics(startTime, endTime);
        log.info("查询管理端举报统计成功，total={}", statisticsVO.getTotal());
        return HttpResult.success("查询成功", statisticsVO);
    }
}
