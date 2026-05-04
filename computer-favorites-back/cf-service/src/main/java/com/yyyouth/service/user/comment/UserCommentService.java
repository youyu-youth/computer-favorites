package com.yyyouth.service.user.comment;

import com.yyyouth.model.dto.user.CommentCreateDTO;
import com.yyyouth.model.dto.user.CommentPageQueryDTO;
import com.yyyouth.model.dto.user.MyCommentPageQueryDTO;
import com.yyyouth.model.vo.user.CommentPageVO;
import com.yyyouth.model.vo.user.MyCommentPageVO;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 用户评论服务接口
 */
public interface UserCommentService {

    /**
     * 发布评论
     *
     * @param websiteId 网站ID
     * @param dto 评论参数
     * @return 新评论ID
     */
    Long publishComment(Long websiteId, CommentCreateDTO dto);

    /**
     * 分页查询评论列表（两级折叠）
     *
     * @param websiteId 网站ID
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    CommentPageVO pageComments(Long websiteId, CommentPageQueryDTO queryDTO);

    /**
     * 删除评论（逻辑删除）
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     */
    void unlikeComment(Long commentId);

    /**
     * 分页查询我的评论
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    MyCommentPageVO pageMyComments(MyCommentPageQueryDTO queryDTO);
}
