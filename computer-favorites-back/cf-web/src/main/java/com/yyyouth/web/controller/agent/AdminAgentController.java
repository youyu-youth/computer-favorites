package com.yyyouth.web.controller.agent;

import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.agent.AgentChatRequest;
import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.model.pojo.agent.AgentSkill;
import com.yyyouth.model.vo.agent.AgentQuotaVO;
import com.yyyouth.model.vo.agent.AgentSessionVO;
import com.yyyouth.service.aichat.chat.ChatOrchestrator;
import com.yyyouth.service.aichat.quota.QuotaGuard;
import com.yyyouth.service.aichat.rag.AgentKnowledgeService;
import com.yyyouth.service.aichat.session.AgentSessionService;
import com.yyyouth.service.aichat.skill.AgentSkillService;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    private final AgentSessionService sessionService;
    private final QuotaGuard quotaGuard;
    private final AgentSkillService skillService;
    private final AgentKnowledgeService knowledgeService;

    @PostMapping(value = "/chat")
    public SseEmitter chat(@RequestBody @Valid AgentChatRequest request) {
        StpAdminUtil.checkLogin();
        Long adminId = StpAdminUtil.getLoginIdAsLong();

        log.info("管理端 Agent 对话: adminId={}, skillCode={}, message={}",
                adminId, request.getSkillCode(), request.getMessage());

        SseEmitter emitter = new SseEmitter(300_000L);

        emitter.onTimeout(() -> log.warn("SSE 连接超时: adminId={}, conversationId={}",
                adminId, request.getConversationId()));
        emitter.onError(ex -> log.error("SSE 连接错误: adminId={}", adminId, ex));

        try {
            chatOrchestrator.handle("admin", adminId, request.getMessage(),
                    request.getSkillCode(), request.getConversationId(), emitter);
        } catch (Exception e) {
            log.error("管理端 Agent 对话处理异常: adminId={}, conversationId={}",
                    adminId, request.getConversationId(), e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("code", "INTERNAL_ERROR", "msg", e.getMessage())));
            } catch (IOException ignored) {
            }
            emitter.complete();
        }

        return emitter;
    }

    /**
     * 获取管理员会话列表
     *
     * @param keyword 搜索关键词（可选）
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public HttpResult getSessions(@RequestParam(required = false) String keyword) {
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        List<AgentSession> sessions = sessionService.listByOwner("admin", adminId, keyword);
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
     * 查询管理员配额
     *
     * @return 配额信息
     */
    @GetMapping("/quota")
    public HttpResult getQuota() {
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        AgentQuotaVO quota = quotaGuard.checkAndGetQuota("admin", adminId);
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

    /**
     * 获取技能列表
     */
    @GetMapping("/skills")
    public HttpResult getSkills() {
        List<AgentSkill> skills = skillService.listAll();
        return HttpResult.success(skills);
    }

    /**
     * 新建技能
     */
    @PostMapping("/skills")
    public HttpResult createSkill(@RequestBody AgentSkill skill) {
        skillService.create(skill);
        return HttpResult.success();
    }

    /**
     * 编辑技能
     */
    @PutMapping("/skills/{id}")
    public HttpResult updateSkill(@PathVariable Long id, @RequestBody AgentSkill skill) {
        skillService.update(id, skill);
        return HttpResult.success();
    }

    /**
     * 启禁技能
     */
    @PutMapping("/skills/{id}/status")
    public HttpResult toggleSkillStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        skillService.toggleStatus(id, body.getOrDefault("enabled", true));
        return HttpResult.success();
    }

    /**
     * 同步网站数据到 RAG 向量库
     */
    @PostMapping("/knowledge/sync")
    public HttpResult syncKnowledge() {
        int count = knowledgeService.syncWebsites();
        return HttpResult.success(Map.of("syncedWebsites", count));
    }
}
