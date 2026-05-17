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
 * Agent MCP服务配置表实体
 */
@Data
@TableName("t_agent_mcp_server")
public class AgentMcpServer {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * MCP服务编码
     */
    private String serverCode;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 传输协议：streamable_http/sse/stdio
     */
    private String transport;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 端点路径
     */
    private String endpoint;

    /**
     * 认证类型：none/api_key/bearer
     */
    private String authType;

    /**
     * 密钥引用
     */
    private String secretRef;

    /**
     * 是否启用 1启用 0禁用
     */
    private Integer enabled;

    /**
     * 超时时间
     */
    private Integer timeoutMs;

    /**
     * 扩展元数据
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
}
