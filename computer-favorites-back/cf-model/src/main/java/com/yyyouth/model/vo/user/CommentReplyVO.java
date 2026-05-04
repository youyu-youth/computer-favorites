package com.yyyouth.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 评论回复视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReplyVO {

    /**
     * 回复ID
     */
    private Long id;

    /**
     * 回复用户
     */
    private CommentUserVO user;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 回复是否已删除
     */
    private Boolean isDeleted;

    /**
     * 被回复用户昵称
     */
    private String replyTo;
}
