package com.yyyouth.service.aichat.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * 用户消息意图路由器：闲聊 vs 功能请求 二分类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentRouter {

    private final ChatClient plainDashscopeChatClient;

    private static final String CLASSIFY_PROMPT = """
            判断用户消息属于以下哪类：
            - chitchat: 日常问候、闲聊、无关痛痒的对话
            - functional: 涉及网站收录、数据分析、排行榜、投稿审核等系统功能

            仅回复一个单词：chitchat 或 functional。不要输出任何其他内容。""";

    /**
     * 分类用户消息意图
     *
     * @param message 用户消息
     * @return "chitchat" 或 "functional"
     */
    public String classify(String message) {
        try {
            String result = plainDashscopeChatClient.prompt()
                    .system(CLASSIFY_PROMPT)
                    .user(message)
                    .call()
                    .content();
            if (result == null) {
                return "chitchat";
            }
            String trimmed = result.trim().toLowerCase();
            if (trimmed.contains("functional")) {
                log.info("[IntentRouter] 分类结果: functional, raw={}", trimmed);
                return "functional";
            }
            log.info("[IntentRouter] 分类结果: chitchat, raw={}", trimmed);
            return "chitchat";
        } catch (Exception e) {
            log.warn("[IntentRouter] 分类调用失败，默认走闲聊: {}", e.getMessage());
            return "chitchat";
        }
    }
}
