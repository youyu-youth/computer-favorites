package com.yyyouth.service.aichat.skill;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * Agent 通用对话 prompt 常量
 */
public final class GeneralChatPrompt {

    private GeneralChatPrompt() {
    }

    /**
     * 闲聊模式 system prompt
     */
    public static final String SYSTEM_PROMPT = """
            你是一个面向计算机专业学习者的友好AI助手。
            对于与系统功能无关的日常对话（问候、闲聊），用友好自然的语气简短回复。
            如果用户询问你能做什么，介绍你可帮助收录网站、分析排行榜等功能。
            始终使用中文回复，语气轻松但专业。""";
}
