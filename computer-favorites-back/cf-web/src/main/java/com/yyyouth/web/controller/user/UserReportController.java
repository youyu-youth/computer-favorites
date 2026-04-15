package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.ReportQueryDTO;
import com.yyyouth.model.dto.user.ReportSubmitDTO;
import com.yyyouth.model.vo.user.ReportDetailVO;
import com.yyyouth.model.vo.user.ReportListItemVO;
import com.yyyouth.model.vo.user.ReportSubmitVO;
import com.yyyouth.service.user.report.UserReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 用户举报接口控制器
 */
@Api(tags = "用户举报接口")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/report")
public class UserReportController {

    private final UserReportService userReportService;

    /**
     * 提交举报
     *
     * @param submitDTO 举报提交参数
     * @return 举报提交结果
     */
    @ApiOperation(value = "提交举报")
    @SaCheckLogin
    @PostMapping
    public HttpResult submitReport(@RequestBody @Valid ReportSubmitDTO submitDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        ReportSubmitVO result = userReportService.submitReport(submitDTO, userId);
        return HttpResult.success("举报提交成功", result);
    }

    /**
     * 我的举报列表
     *
     * @param queryDTO 查询参数
     * @return 举报列表分页数据
     */
    @ApiOperation(value = "我的举报列表")
    @SaCheckLogin
    @GetMapping("/my")
    public HttpResult getMyReports(@Valid ReportQueryDTO queryDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<ReportListItemVO> result = userReportService.getMyReports(queryDTO, userId);
        return HttpResult.success("查询成功", result);
    }

    /**
     * 举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    @ApiOperation(value = "举报详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public HttpResult getReportDetail(@PathVariable("id") @NotNull(message = "举报ID不能为空") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        ReportDetailVO result = userReportService.getReportDetail(id, userId);
        return HttpResult.success("查询成功", result);
    }
}
