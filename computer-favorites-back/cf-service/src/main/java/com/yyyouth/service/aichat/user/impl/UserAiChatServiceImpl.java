package com.yyyouth.service.aichat.user.impl;

import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import com.yyyouth.service.aichat.user.UserAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAiChatServiceImpl implements UserAiChatService {

    private final ChatOrchestrator chatOrchestrator;

    @Override
    public String chatTest(String userPrompt) {
        log.info("测试对话: {}", userPrompt);
        return "Agent 框架已就绪，请使用 SSE 端点 /api/agent/user/chat 进行对话";
    }
}
