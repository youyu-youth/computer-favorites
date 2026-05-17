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
 * Agent钩子事件表实体
 */
@Data
@TableName("t_agent_hook_event")
public class AgentHookEvent {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent任务ID
     */
    private Long taskId;

    /**
     * 钩子类型
     */
    private String hookType;

    /**
     * 处理器编码
     */
    private String handlerCode;

    /**
     * 事件数据
     */
    private String eventPayloadJson;

    /**
     * 状态
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
