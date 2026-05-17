package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent技能模块表实体
 */
@Data
@TableName("t_agent_skill")
public class AgentSkill {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 技能编码
     */
    private String skillCode;

    /**
     * 技能名称
     */
    private String name;

    /**
     * 适用端：user/admin/both
     */
    private String audience;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 本地工具白名单
     */
    private String toolAllowlistJson;

    /**
     * MCP白名单
     */
    private String mcpAllowlistJson;

    /**
     * 是否启用 1启用 0禁用
     */
    private Integer enabled;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
