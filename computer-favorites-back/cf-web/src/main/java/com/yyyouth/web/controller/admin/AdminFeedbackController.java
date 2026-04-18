package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminFeedbackQueryDTO;
import com.yyyouth.model.dto.admin.AdminFeedbackReplyDTO;
import com.yyyouth.model.vo.admin.AdminFeedbackDetailVO;
import com.yyyouth.model.vo.admin.AdminFeedbackHandleResultVO;
import com.yyyouth.model.vo.admin.AdminFeedbackPageVO;
import com.yyyouth.service.admin.feedback.AdminFeedbackService;
import com.yyyouth.service.audit.annotation.AuditLog;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈处理控制器
 */
@Slf4j
@Api(tags = "管理端反馈处理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/feedback")
@AuditLog(module = "admin-feedback")
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    /**
     * 查询反馈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询反馈分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:feedback:list", type = "admin")
    public HttpResult list(@Valid AdminFeedbackQueryDTO queryDTO) {
        log.info("查询管理端反馈列表请求，pageNum={}, pageSize={}, status={}, type={}, hasImages={}, hasContact={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getStatus(),
                queryDTO.getType(), queryDTO.getHasImages(), queryDTO.getHasContact());
        AdminFeedbackPageVO pageVO = adminFeedbackService.queryFeedbackPage(queryDTO);
        log.info("查询管理端反馈列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    @ApiOperation(value = "查询反馈详情")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "admin:feedback:detail", type = "admin")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("查询管理端反馈详情请求，id={}", id);
        AdminFeedbackDetailVO detailVO = adminFeedbackService.queryFeedbackDetail(id);
        log.info("查询管理端反馈详情成功，id={}", id);
        return HttpResult.success("查询成功", detailVO);
    }

    /**
     * 回复反馈
     *
     * @param id 反馈ID
     * @param replyDTO 回复参数
     * @return 处理结果
     */
    @ApiOperation(value = "回复反馈")
    @PutMapping("/{id}/reply")
    @SaCheckPermission(value = "admin:feedback:reply", type = "admin")
    @AuditLog(action = "reply", description = "回复反馈，feedbackId=#{#id}")
    public HttpResult reply(@PathVariable("id") @NotNull @Positive Long id,
                            @RequestBody @Valid @NotNull AdminFeedbackReplyDTO replyDTO) {
        log.info("管理端回复反馈请求，id={}", id);
        AdminFeedbackHandleResultVO resultVO = adminFeedbackService.replyFeedback(id, replyDTO);
        log.info("管理端回复反馈成功，id={}, status={}", id, resultVO.getStatus());
        return HttpResult.success("回复成功", resultVO);
    }

    /**
     * 关闭反馈
     *
     * @param id 反馈ID
     * @return 处理结果
     */
    @ApiOperation(value = "关闭反馈")
    @PutMapping("/{id}/close")
    @SaCheckPermission(value = "admin:feedback:close", type = "admin")
    @AuditLog(action = "close", description = "关闭反馈，feedbackId=#{#id}")
    public HttpResult close(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端关闭反馈请求，id={}", id);
        AdminFeedbackHandleResultVO resultVO = adminFeedbackService.closeFeedback(id);
        log.info("管理端关闭反馈成功，id={}, status={}", id, resultVO.getStatus());
        return HttpResult.success("关闭成功", resultVO);
    }
}
