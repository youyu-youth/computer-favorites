package com.yyyouth.web.controller.agent;

import cn.dev33.satoken.stp.StpAdminUtil;
import com.yyyouth.model.dto.agent.AgentChatRequest;
import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 管理端 AI Agent SSE 对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/agent")
@RequiredArgsConstructor
public class AdminAgentController {

    private final ChatOrchestrator chatOrchestrator;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid AgentChatRequest request) {
        StpAdminUtil.checkLogin();
        Long adminId = StpAdminUtil.getLoginIdAsLong();

        log.info("管理端 Agent 对话: adminId={}, skillCode={}, message={}",
                adminId, request.getSkillCode(), request.getMessage());

        SseEmitter emitter = new SseEmitter(300_000L);

        chatOrchestrator.handle("admin", adminId, request.getMessage(),
                request.getSkillCode(), request.getConversationId(), emitter);

        return emitter;
    }
}
