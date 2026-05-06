package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息条目
 */
@Data
public class UserMessageItemVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 关联网站ID（评论回复类型时有效）
     */
    private Long websiteId;

    /**
     * 已读状态：0未读，1已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
