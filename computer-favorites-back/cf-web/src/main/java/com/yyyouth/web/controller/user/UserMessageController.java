package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserMessageBatchReadDTO;
import com.yyyouth.model.dto.user.UserMessageQueryDTO;
import com.yyyouth.model.vo.user.UserMessageBatchReadResultVO;
import com.yyyouth.model.vo.user.UserMessagePageVO;
import com.yyyouth.service.user.notification.UserMessageService;
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
 * @date 2026-04-08
 *
 * 用户消息控制器
 */
@Slf4j
@Api(tags = "用户消息接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/messages")
public class UserMessageController {

    private final UserMessageService userMessageService;

    /**
     * 查询当前登录用户消息分页
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "查询当前登录用户消息分页")
    @GetMapping
    @SaCheckLogin
    public HttpResult queryMessagePage(@Valid UserMessageQueryDTO queryDTO) {
        log.info("收到用户消息分页查询请求，pageNum={}, pageSize={}, isRead={}, type={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getIsRead(), queryDTO.getType());
        UserMessagePageVO pageVO = userMessageService.queryMessagePage(queryDTO);
        log.info("用户消息分页查询完成，total={}, unreadCount={}", pageVO.getTotal(), pageVO.getUnreadCount());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 标记单条消息已读
     *
     * @param messageId 消息ID
     * @return 执行结果
     */
    @ApiOperation(value = "标记单条消息已读")
    @PutMapping("/{messageId}/read")
    @SaCheckLogin
    public HttpResult markMessageRead(@PathVariable("messageId") @NotNull(message = "消息ID不能为空") @Positive(message = "消息ID必须为正数") Long messageId) {
        log.info("收到单条消息已读请求，messageId={}", messageId);
        userMessageService.markMessageRead(messageId);
        log.info("单条消息已读完成，messageId={}", messageId);
        return HttpResult.success("标记已读成功");
    }

    /**
     * 批量标记消息已读
     *
     * @param batchReadDTO 批量参数
     * @return 批量结果
     */
    @ApiOperation(value = "批量标记消息已读")
    @PutMapping("/read/batch")
    @SaCheckLogin
    public HttpResult batchReadMessages(@RequestBody @Valid UserMessageBatchReadDTO batchReadDTO) {
        log.info("收到批量消息已读请求，size={}", batchReadDTO.getIds().size());
        UserMessageBatchReadResultVO resultVO = userMessageService.batchReadMessages(batchReadDTO);
        log.info("批量消息已读完成，successCount={}, failedCount={}",
                resultVO.getSuccessCount(), resultVO.getFailedCount());
        return HttpResult.success("批量操作完成", resultVO);
    }
}
