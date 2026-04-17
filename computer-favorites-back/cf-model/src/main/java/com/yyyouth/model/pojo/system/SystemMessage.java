package com.yyyouth.model.pojo.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 站内消息实体
 */
@Data
@TableName("t_message")
public class SystemMessage {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息类型：1系统通知，2评论回复，3收藏提醒，4审核结果，5举报反馈
     */
    private Integer type;

    /**
     * 关联业务ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
