package com.yyyouth.service.user.notification.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.yyyouth.common.constants.NotificationErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserMessageBatchReadDTO;
import com.yyyouth.model.dto.user.UserMessageQueryDTO;
import com.yyyouth.model.pojo.system.SystemMessage;
import com.yyyouth.model.vo.user.UserMessageBatchReadResultVO;
import com.yyyouth.model.vo.user.UserMessageItemVO;
import com.yyyouth.model.vo.user.UserMessagePageVO;
import com.yyyouth.service.mapper.system.SystemMessageMapper;
import com.yyyouth.service.user.notification.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserMessageServiceImpl implements UserMessageService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MESSAGE_READ = 1;

    private static final int MESSAGE_BATCH_LIMIT = 100;

    private final SystemMessageMapper systemMessageMapper;

    /**
     * 查询当前登录用户消息分页
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public UserMessagePageVO queryMessagePage(UserMessageQueryDTO queryDTO) {
        Long loginUserId = getLoginUserId();
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        Long total = systemMessageMapper.countUserMessages(loginUserId, queryDTO.getIsRead(), queryDTO.getType());
        Long unreadCount = systemMessageMapper.countUnreadMessages(loginUserId);

        UserMessagePageVO pageVO = new UserMessagePageVO();
        pageVO.setTotal(total == null ? 0L : total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setUnreadCount(unreadCount == null ? 0L : unreadCount);

        if (total == null || total == 0L) {
            pageVO.setList(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        List<SystemMessage> messages = systemMessageMapper.selectUserMessagePage(
                loginUserId,
                queryDTO.getIsRead(),
                queryDTO.getType(),
                offset,
                pageSize
        );

        List<UserMessageItemVO> messageItems = messages.stream()
                .map(message -> BeanUtil.copyProperties(message, UserMessageItemVO.class))
                .toList();
        pageVO.setList(messageItems);
        return pageVO;
    }

    /**
     * 标记单条消息已读
     *
     * @param messageId 消息ID
     */
    @Override
    public void markMessageRead(Long messageId) {
        Long loginUserId = getLoginUserId();
        SystemMessage message = systemMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(
                    NotificationErrorCode.MESSAGE_NOT_FOUND.getCode(),
                    NotificationErrorCode.MESSAGE_NOT_FOUND.getMessage()
            );
        }

        if (!Objects.equals(message.getUserId(), loginUserId)) {
            throw new BusinessException(
                    NotificationErrorCode.MESSAGE_OWNER_MISMATCH.getCode(),
                    NotificationErrorCode.MESSAGE_OWNER_MISMATCH.getMessage()
            );
        }

        if (Objects.equals(message.getIsRead(), MESSAGE_READ)) {
            return;
        }

        SystemMessage updateEntity = new SystemMessage();
        updateEntity.setId(messageId);
        updateEntity.setIsRead(MESSAGE_READ);
        int affectedRows = systemMessageMapper.updateById(updateEntity);
        if (affectedRows <= 0) {
            throw new BusinessException(
                    NotificationErrorCode.MESSAGE_NOT_FOUND.getCode(),
                    NotificationErrorCode.MESSAGE_NOT_FOUND.getMessage()
            );
        }
    }

    /**
     * 批量标记消息已读
     *
     * @param batchReadDTO 批量参数
     * @return 批量处理结果
     */
    @Override
    public UserMessageBatchReadResultVO batchReadMessages(UserMessageBatchReadDTO batchReadDTO) {
        Long loginUserId = getLoginUserId();
        List<Long> normalizedMessageIds = normalizeMessageIds(batchReadDTO.getIds());
        if (CollUtil.isEmpty(normalizedMessageIds)) {
            throw new BusinessException(
                    NotificationErrorCode.MESSAGE_BATCH_EMPTY.getCode(),
                    NotificationErrorCode.MESSAGE_BATCH_EMPTY.getMessage()
            );
        }

        if (normalizedMessageIds.size() > MESSAGE_BATCH_LIMIT) {
            throw new BusinessException(
                    NotificationErrorCode.MESSAGE_BATCH_LIMIT_EXCEEDED.getCode(),
                    NotificationErrorCode.MESSAGE_BATCH_LIMIT_EXCEEDED.getMessage()
            );
        }

        List<SystemMessage> messages = systemMessageMapper.selectBatchIds(normalizedMessageIds);
        Map<Long, SystemMessage> messageMap = buildMessageMap(messages);
        List<Long> failedIds = new ArrayList<>();
        int successCount = 0;

        for (Long messageId : normalizedMessageIds) {
            SystemMessage message = messageMap.get(messageId);
            if (message == null) {
                failedIds.add(messageId);
                continue;
            }

            if (!Objects.equals(message.getUserId(), loginUserId)) {
                failedIds.add(messageId);
                continue;
            }

            if (Objects.equals(message.getIsRead(), MESSAGE_READ)) {
                successCount++;
                continue;
            }

            SystemMessage updateEntity = new SystemMessage();
            updateEntity.setId(messageId);
            updateEntity.setIsRead(MESSAGE_READ);
            int affectedRows = systemMessageMapper.updateById(updateEntity);
            if (affectedRows > 0) {
                successCount++;
            } else {
                failedIds.add(messageId);
            }
        }

        UserMessageBatchReadResultVO resultVO = new UserMessageBatchReadResultVO();
        resultVO.setSuccessCount(successCount);
        resultVO.setFailedCount(failedIds.size());
        resultVO.setFailedIds(failedIds);
        return resultVO;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getLoginUserId() {
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 归一化消息ID列表
     *
     * @param messageIds 原始消息ID列表
     * @return 去重后的消息ID列表
     */
    private List<Long> normalizeMessageIds(List<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return Collections.emptyList();
        }
        return messageIds.stream()
                .filter(Objects::nonNull)
                .filter(messageId -> messageId > 0)
                .distinct()
                .toList();
    }

    /**
     * 构建消息ID索引
     *
     * @param messages 消息列表
     * @return 消息索引映射
     */
    private Map<Long, SystemMessage> buildMessageMap(List<SystemMessage> messages) {
        if (CollUtil.isEmpty(messages)) {
            return Collections.emptyMap();
        }

        Map<Long, SystemMessage> messageMap = new HashMap<>(messages.size());
        for (SystemMessage message : messages) {
            if (message.getId() != null) {
                messageMap.put(message.getId(), message);
            }
        }
        return messageMap;
    }
}
