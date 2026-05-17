package com.yyyouth.service.aichat.skill;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * 技能运行时定义
 */
@Data
@Builder
public class SkillDefinition {
    private String skillCode;
    private String name;
    private String audience;
    private String systemPrompt;
    private List<String> toolAllowlist;
    private List<String> mcpAllowlist;
}
