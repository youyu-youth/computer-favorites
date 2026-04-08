package com.yyyouth.service.user.notification;

import com.yyyouth.model.dto.user.UserMessageBatchReadDTO;
import com.yyyouth.model.dto.user.UserMessageQueryDTO;
import com.yyyouth.model.vo.user.UserMessageBatchReadResultVO;
import com.yyyouth.model.vo.user.UserMessagePageVO;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息服务接口
 */
public interface UserMessageService {

	/**
	 * 查询当前登录用户消息分页
	 *
	 * @param queryDTO 查询参数
	 * @return 分页结果
	 */
	UserMessagePageVO queryMessagePage(UserMessageQueryDTO queryDTO);

	/**
	 * 标记单条消息已读
	 *
	 * @param messageId 消息ID
	 */
	void markMessageRead(Long messageId);

	/**
	 * 批量标记消息已读
	 *
	 * @param batchReadDTO 批量参数
	 * @return 批量处理结果
	 */
	UserMessageBatchReadResultVO batchReadMessages(UserMessageBatchReadDTO batchReadDTO);
}
