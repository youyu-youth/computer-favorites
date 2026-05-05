package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminCommentBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminCommentQueryDTO;
import com.yyyouth.model.dto.admin.AdminCommentStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminCommentBatchStatusResultVO;
import com.yyyouth.model.vo.admin.AdminCommentDetailVO;
import com.yyyouth.model.vo.admin.AdminCommentPageVO;
import com.yyyouth.model.vo.admin.AdminCommentStatisticsVO;
import com.yyyouth.service.admin.comment.AdminCommentService;
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
 * @date 2026-05-04
 *
 * 管理端评论管理控制器
 */
@Slf4j
@Api(tags = "管理端评论管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/comments")
@AuditLog(module = "admin-comment")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    /**
     * 查询评论分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询评论分页列表")
    @GetMapping
    @SaCheckPermission(value = "admin:comment:list", type = "admin")
    public HttpResult list(@Valid AdminCommentQueryDTO queryDTO) {
        log.info("查询管理端评论列表请求，pageNum={}, pageSize={}, status={}, keyword={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getStatus(), queryDTO.getKeyword());
        AdminCommentPageVO pageVO = adminCommentService.queryCommentPage(queryDTO);
        log.info("查询管理端评论列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询评论统计概览
     *
     * @return 统计数据
     */
    @ApiOperation(value = "查询评论统计概览")
    @GetMapping("/statistics")
    @SaCheckPermission(value = "admin:comment:statistics", type = "admin")
    public HttpResult statistics() {
        log.info("查询管理端评论统计请求");
        AdminCommentStatisticsVO statisticsVO = adminCommentService.queryCommentStatistics();
        log.info("查询管理端评论统计成功，total={}", statisticsVO.getTotal());
        return HttpResult.success("查询成功", statisticsVO);
    }

    /**
     * 查询评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    @ApiOperation(value = "查询评论详情")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "admin:comment:detail", type = "admin")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("查询管理端评论详情请求，id={}", id);
        AdminCommentDetailVO detailVO = adminCommentService.queryCommentDetail(id);
        log.info("查询管理端评论详情成功，id={}", id);
        return HttpResult.success("查询成功", detailVO);
    }

    /**
     * 单条评论状态变更（隐藏/显示）
     *
     * @param id 评论ID
     * @param statusDTO 状态变更参数
     * @return 操作结果
     */
    @ApiOperation(value = "单条评论状态变更")
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = "admin:comment:status", type = "admin")
    @AuditLog(action = "update-status", description = "变更评论状态，commentId=#{#id}，status=#{#statusDTO.status}")
    public HttpResult updateStatus(@PathVariable("id") @NotNull @Positive Long id,
                                   @RequestBody @Valid @NotNull AdminCommentStatusUpdateDTO statusDTO) {
        log.info("管理端变更评论状态请求，commentId={}, status={}, reason={}", id, statusDTO.getStatus(), statusDTO.getReason());
        adminCommentService.updateCommentStatus(id, statusDTO);
        log.info("管理端变更评论状态成功，commentId={}, status={}", id, statusDTO.getStatus());
        return HttpResult.success("操作成功");
    }

    /**
     * 批量评论状态变更（隐藏/显示）
     *
     * @param batchDTO 批量状态变更参数
     * @return 批量操作结果
     */
    @ApiOperation(value = "批量评论状态变更")
    @PutMapping("/batch/status")
    @SaCheckPermission(value = "admin:comment:status", type = "admin")
    @AuditLog(action = "batch-update-status", description = "批量变更评论状态，count=#{#batchDTO.ids.size()}，status=#{#batchDTO.status}")
    public HttpResult batchUpdateStatus(@RequestBody @Valid @NotNull AdminCommentBatchStatusUpdateDTO batchDTO) {
        log.info("管理端批量变更评论状态请求，count={}, status={}", batchDTO.getIds().size(), batchDTO.getStatus());
        AdminCommentBatchStatusResultVO resultVO = adminCommentService.batchUpdateCommentStatus(batchDTO);
        log.info("管理端批量变更评论状态完成，success={}, skipped={}", resultVO.getSuccess(), resultVO.getSkipped());
        return HttpResult.success(resultVO.getMessage(), resultVO);
    }
}
