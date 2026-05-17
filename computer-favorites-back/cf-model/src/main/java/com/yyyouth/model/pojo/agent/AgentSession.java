package com.yyyouth.model.pojo.agent;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent会话主表实体
 */
@Data
@TableName("t_agent_session")
public class AgentSession {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Spring AI 会话ID
     */
    private String conversationId;

    /**
     * 兼容关联 t_ai_conversation.id
     */
    private Long aiConversationId;

    /**
     * 主体类型：user/admin
     */
    private String ownerType;

    /**
     * 主体ID
     */
    private Long ownerId;

    /**
     * Agent编码
     */
    private String agentCode;

    /**
     * 技能编码
     */
    private String skillCode;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 状态：active/archived/closed
     */
    private String status;

    /**
     * 模型供应商：deepseek/dashscope
     */
    private String modelProvider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 最近消息时间
     */
    private LocalDateTime lastMessageAt;

    /**
     * 扩展上下文
     */
    private String metadataJson;

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

    /**
     * 逻辑删除 0未删除 1已删除
     */
    @TableLogic
    private Integer deleted;
}
