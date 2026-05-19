package com.yyyouth.service.aichat.session;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.service.mapper.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 会话管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;

    public AgentSession getOrCreate(String conversationId, String ownerType, Long ownerId,
                                     String agentCode, String skillCode, String modelProvider, String modelName) {
        if (conversationId != null && !conversationId.isBlank()) {
            log.info("getOrCreate: 查询已有会话 conversationId={}", conversationId);
            AgentSession existing = agentSessionMapper.selectOne(
                    new LambdaQueryWrapper<AgentSession>()
                            .eq(AgentSession::getConversationId, conversationId));
            if (existing != null) {
                log.info("getOrCreate: 找到已有会话 id={}", existing.getId());
                existing.setLastMessageAt(LocalDateTime.now());
                agentSessionMapper.updateById(existing);
                return existing;
            }
            log.info("getOrCreate: conversationId={} 未找到，将创建新会话", conversationId);
        }

        log.info("getOrCreate: 开始创建新会话 ownerType={} ownerId={}", ownerType, ownerId);
        AgentSession session = new AgentSession();
        String newConvId = UUID.fastUUID().toString();
        session.setConversationId(newConvId);
        session.setOwnerType(ownerType);
        session.setOwnerId(ownerId);
        session.setAgentCode(agentCode);
        session.setSkillCode(skillCode);
        session.setStatus("active");
        session.setModelProvider(modelProvider);
        session.setModelName(modelName);
        session.setLastMessageAt(LocalDateTime.now());
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        log.info("getOrCreate: 准备 insert, conversationId={}", newConvId);
        agentSessionMapper.insert(session);
        log.info("getOrCreate: insert 完成, id={}", session.getId());

        log.info("创建 Agent 会话: conversationId={}, ownerType={}, ownerId={}",
                session.getConversationId(), ownerType, ownerId);
        return session;
    }

    public void archive(Long sessionId) {
        AgentSession session = agentSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus("archived");
            agentSessionMapper.updateById(session);
        }
    }

    /**
     * 查询指定用户/管理员的所有活跃会话列表
     *
     * @param ownerType 用户类型 user/admin
     * @param ownerId   用户ID
     * @param keyword   搜索关键词（可选，过滤 title）
     * @return 会话列表（置顶在前，按 lastMessageAt 倒序）
     */
    public List<AgentSession> listByOwner(String ownerType, Long ownerId, String keyword) {
        LambdaQueryWrapper<AgentSession> wrapper = new LambdaQueryWrapper<AgentSession>()
                .eq(AgentSession::getOwnerType, ownerType)
                .eq(AgentSession::getOwnerId, ownerId)
                .eq(AgentSession::getStatus, "active")
                .orderByDesc(AgentSession::getPinned)
                .orderByDesc(AgentSession::getLastMessageAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(AgentSession::getTitle, keyword);
        }
        return agentSessionMapper.selectList(wrapper);
    }

    /**
     * 重命名会话
     *
     * @param sessionId 会话ID
     * @param title     新标题
     */
    public void rename(Long sessionId, String title) {
        AgentSession session = new AgentSession();
        session.setId(sessionId);
        session.setTitle(title);
        agentSessionMapper.updateById(session);
    }

    /**
     * 软删除会话
     *
     * @param sessionId 会话ID
     */
    public void delete(Long sessionId) {
        agentSessionMapper.deleteById(sessionId);
    }

    /**
     * 回填会话的 skill_code（LLM 匹配技能后调用）
     *
     * @param sessionId 会话ID
     * @param skillCode 技能编码
     */
    public void updateSkillCode(Long sessionId, String skillCode) {
        AgentSession session = new AgentSession();
        session.setId(sessionId);
        session.setSkillCode(skillCode);
        agentSessionMapper.updateById(session);
    }

    /**
     * 切换会话置顶状态
     *
     * @param sessionId 会话ID
     * @param pinned    是否置顶
     */
    public void togglePin(Long sessionId, boolean pinned) {
        AgentSession session = new AgentSession();
        session.setId(sessionId);
        session.setPinned(pinned ? 1 : 0);
        agentSessionMapper.updateById(session);
    }

    /**
     * 按 ID 查询会话
     *
     * @param sessionId 会话ID
     * @return 会话实体
     */
    public AgentSession getById(Long sessionId) {
        return agentSessionMapper.selectById(sessionId);
    }
}
