package com.yyyouth.service.aichat.skill;

import com.yyyouth.model.pojo.agent.AgentSkill;
import com.yyyouth.service.mapper.AgentSkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-19
 *
 * Agent 技能管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentSkillService {

    private final AgentSkillMapper skillMapper;
    private final SkillLoader skillLoader;

    public List<AgentSkill> listAll() {
        return skillMapper.selectList(null);
    }

    public List<SkillDefinition> listForAudience(String audience) {
        return skillLoader.getByAudience(audience);
    }

    public AgentSkill create(AgentSkill skill) {
        skillMapper.insert(skill);
        skillLoader.refresh();
        log.info("技能创建成功: code={}", skill.getSkillCode());
        return skill;
    }

    public AgentSkill update(Long id, AgentSkill skill) {
        skill.setId(id);
        skillMapper.updateById(skill);
        skillLoader.refresh();
        log.info("技能更新成功: id={}", id);
        return skill;
    }

    public void toggleStatus(Long id, boolean enabled) {
        AgentSkill skill = skillMapper.selectById(id);
        if (skill != null) {
            skill.setEnabled(enabled ? 1 : 0);
            skillMapper.updateById(skill);
            skillLoader.refresh();
            log.info("技能状态变更: id={}, enabled={}", id, enabled);
        }
    }
}
