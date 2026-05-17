package com.yyyouth.service.aichat.session;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.agent.AgentSession;
import com.yyyouth.service.mapper.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
            AgentSession existing = agentSessionMapper.selectOne(
                    new LambdaQueryWrapper<AgentSession>()
                            .eq(AgentSession::getConversationId, conversationId));
            if (existing != null) {
                existing.setLastMessageAt(LocalDateTime.now());
                agentSessionMapper.updateById(existing);
                return existing;
            }
        }

        AgentSession session = new AgentSession();
        session.setConversationId(UUID.fastUUID().toString());
        session.setOwnerType(ownerType);
        session.setOwnerId(ownerId);
        session.setAgentCode(agentCode);
        session.setSkillCode(skillCode);
        session.setStatus("active");
        session.setModelProvider(modelProvider);
        session.setModelName(modelName);
        session.setLastMessageAt(LocalDateTime.now());
        agentSessionMapper.insert(session);

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
}
