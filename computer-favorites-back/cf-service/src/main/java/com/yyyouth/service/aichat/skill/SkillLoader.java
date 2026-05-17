package com.yyyouth.service.aichat.skill;

import com.alibaba.fastjson2.JSON;
import com.yyyouth.model.pojo.agent.AgentSkill;
import com.yyyouth.service.mapper.AgentSkillMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 技能加载器：从 DB 加载技能定义，维护内存缓存，支持定时刷新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillLoader {

    private final AgentSkillMapper agentSkillMapper;

    private final Map<String, SkillDefinition> skillCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRate = 300_000)
    public void refresh() {
        List<AgentSkill> skills = agentSkillMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AgentSkill>()
                        .eq(AgentSkill::getEnabled, 1));

        skillCache.clear();
        for (AgentSkill skill : skills) {
            List<String> toolAllowlist = JSON.parseArray(skill.getToolAllowlistJson(), String.class);
            List<String> mcpAllowlist = JSON.parseArray(skill.getMcpAllowlistJson(), String.class);

            SkillDefinition def = SkillDefinition.builder()
                    .skillCode(skill.getSkillCode())
                    .name(skill.getName())
                    .audience(skill.getAudience())
                    .systemPrompt(skill.getSystemPrompt())
                    .toolAllowlist(toolAllowlist != null ? toolAllowlist : List.of())
                    .mcpAllowlist(mcpAllowlist != null ? mcpAllowlist : List.of())
                    .build();
            skillCache.put(skill.getSkillCode(), def);
        }
        log.info("SkillLoader 刷新完成，加载 {} 个技能", skillCache.size());
    }

    public SkillDefinition get(String skillCode) {
        return skillCache.get(skillCode);
    }

    public List<SkillDefinition> getByAudience(String ownerType) {
        return skillCache.values().stream()
                .filter(s -> s.getAudience().equals(ownerType) || s.getAudience().equals("both"))
                .collect(Collectors.toList());
    }
}
