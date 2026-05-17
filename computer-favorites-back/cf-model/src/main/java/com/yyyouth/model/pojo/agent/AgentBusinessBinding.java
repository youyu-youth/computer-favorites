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
 * Agent业务绑定表实体
 */
@Data
@TableName("t_agent_business_binding")
public class AgentBusinessBinding {

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
     * 业务类型：website/report/comment/feedback
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 关系类型：created/reviewed/suggested
     */
    private String relationType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
