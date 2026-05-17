package com.yyyouth.web.controller.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentChatRequest;
import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.model.vo.agent.AgentSessionVO;
import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import com.yyyouth.service.aichat.quota.QuotaGuard;
import com.yyyouth.service.aichat.session.AgentSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 用户端 AI Agent SSE 对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/user")
@RequiredArgsConstructor
public class UserAgentController {

    private final ChatOrchestrator chatOrchestrator;
    private final AgentSessionService sessionService;
    private final QuotaGuard quotaGuard;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid AgentChatRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        log.info("用户端 Agent 对话: userId={}, skillCode={}, message={}",
                userId, request.getSkillCode(), request.getMessage());

        SseEmitter emitter = new SseEmitter(300_000L);

        chatOrchestrator.handle("user", userId, request.getMessage(),
                request.getSkillCode(), request.getConversationId(), emitter);

        return emitter;
    }

    /**
     * 获取当前用户的会话列表
     *
     * @param keyword 搜索关键词（可选）
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public HttpResult getSessions(@RequestParam(required = false) String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AgentSession> sessions = sessionService.listByOwner("user", userId, keyword);
        List<AgentSessionVO> vos = sessions.stream().map(s -> AgentSessionVO.builder()
                .id(s.getId())
                .conversationId(s.getConversationId())
                .title(s.getTitle())
                .skillCode(s.getSkillCode())
                .status(s.getStatus())
                .pinned(s.getPinned())
                .ownerType(s.getOwnerType())
                .ownerId(s.getOwnerId())
                .lastMessageAt(s.getLastMessageAt())
                .createTime(s.getCreateTime())
                .build()).toList();
        return HttpResult.success(vos);
    }

    /**
     * 重命名会话
     *
     * @param id   会话ID
     * @param body 包含新标题的请求体
     * @return 操作结果
     */
    @PutMapping("/sessions/{id}")
    public HttpResult renameSession(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return HttpResult.error("标题不能为空");
        }
        sessionService.rename(id, title);
        return HttpResult.success();
    }

    /**
     * 删除会话
     *
     * @param id 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{id}")
    public HttpResult deleteSession(@PathVariable Long id) {
        sessionService.delete(id);
        return HttpResult.success();
    }

    /**
     * 切换会话置顶状态
     *
     * @param id   会话ID
     * @param body 包含置顶状态的请求体
     * @return 操作结果
     */
    @PutMapping("/sessions/{id}/pin")
    public HttpResult togglePin(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean pinned = body.get("pinned");
        sessionService.togglePin(id, pinned != null && pinned);
        return HttpResult.success();
    }

    /**
     * 查询当前用户配额
     *
     * @return 配额信息
     */
    @GetMapping("/quota")
    public HttpResult getQuota() {
        Long userId = StpUtil.getLoginIdAsLong();
        AgentQuotaVO quota = quotaGuard.checkAndGetQuota("user", userId);
        return HttpResult.success(quota);
    }

    /**
     * 获取会话详情
     *
     * @param id 会话ID
     * @return 会话详情
     */
    @GetMapping("/sessions/{id}/detail")
    public HttpResult getSessionDetail(@PathVariable Long id) {
        AgentSession session = sessionService.getById(id);
        if (session == null) {
            return HttpResult.error("会话不存在");
        }
        AgentSessionVO vo = AgentSessionVO.builder()
                .id(session.getId())
                .conversationId(session.getConversationId())
                .title(session.getTitle())
                .skillCode(session.getSkillCode())
                .status(session.getStatus())
                .pinned(session.getPinned())
                .ownerType(session.getOwnerType())
                .ownerId(session.getOwnerId())
                .lastMessageAt(session.getLastMessageAt())
                .createTime(session.getCreateTime())
                .build();
        return HttpResult.success(vo);
    }
}
