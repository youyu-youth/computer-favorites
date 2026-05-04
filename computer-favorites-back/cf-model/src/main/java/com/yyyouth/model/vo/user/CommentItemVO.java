package com.yyyouth.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 顶级评论视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentItemVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论用户
     */
    private CommentUserVO user;

    /**
     * 评论内容
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
     * 评论是否已删除（内容替换占位）
     */
    private Boolean isDeleted;

    /**
     * 子回复总数
     */
    private Integer replyCount;

    /**
     * 子回复列表
     */
    private List<CommentReplyVO> replies;
}
