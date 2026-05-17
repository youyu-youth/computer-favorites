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
 * Agent工具定义表实体
 */
@Data
@TableName("t_agent_tool_def")
public class AgentToolDef {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工具编码
     */
    private String toolCode;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具类型：local/mcp
     */
    private String toolType;

    /**
     * 适用端：user/admin/both
     */
    private String audience;

    /**
     * 风险等级：low/medium/high
     */
    private String riskLevel;

    /**
     * 权限码
     */
    private String permissionCode;

    /**
     * 业务服务方法
     */
    private String serviceMethod;

    /**
     * 入参Schema
     */
    private String schemaJson;

    /**
     * 是否启用 1启用 0禁用
     */
    private Integer enabled;

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
