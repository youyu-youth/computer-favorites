package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论回复项
 */
@Data
public class AdminCommentReplyItemVO {

    private Long id;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Integer likeCount;

    /**
     * 回复目标用户昵称
     */
    private String replyTo;

    /**
     * 是否已删除
     */
    private Boolean isDeleted;

    private LocalDateTime createTime;
}
