package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.CommentCreateDTO;
import com.yyyouth.model.dto.user.CommentPageQueryDTO;
import com.yyyouth.model.dto.user.MyCommentPageQueryDTO;
import com.yyyouth.model.vo.user.CommentPageVO;
import com.yyyouth.model.vo.user.MyCommentPageVO;
import com.yyyouth.service.user.comment.UserCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 用户评论接口
 */
@Slf4j
@Api(tags = "用户评论接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserCommentController {

    private final UserCommentService userCommentService;

    /**
     * 发布评论
     *
     * @param id 网站ID
     * @param dto 评论参数
     * @return 新评论ID
     */
    @ApiOperation(value = "发布评论")
    @PostMapping("/website/{id}/comments")
    @SaCheckLogin
    public HttpResult publishComment(
            @PathVariable @Positive Long id,
            @RequestBody @Validated CommentCreateDTO dto) {
        log.info("收到发布评论请求，websiteId={}", id);
        Long commentId = userCommentService.publishComment(id, dto);
        log.info("发布评论成功，commentId={}", commentId);
        return HttpResult.success("评论发布成功", commentId);
    }

    /**
     * 评论列表（两级折叠）
     *
     * @param id 网站ID
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "评论列表")
    @GetMapping("/website/{id}/comments")
    public HttpResult listComments(
            @PathVariable @Positive Long id,
            @Validated CommentPageQueryDTO queryDTO) {
        log.info("收到评论列表请求，websiteId={}, pageNum={}", id, queryDTO.getPageNum());
        CommentPageVO pageVO = userCommentService.pageComments(id, queryDTO);
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @ApiOperation(value = "删除评论")
    @DeleteMapping("/comment/{id}")
    @SaCheckLogin
    public HttpResult deleteComment(@PathVariable @Positive Long id) {
        log.info("收到删除评论请求，commentId={}", id);
        userCommentService.deleteComment(id);
        log.info("删除评论成功，commentId={}", id);
        return HttpResult.success("删除成功");
    }

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @ApiOperation(value = "点赞评论")
    @PostMapping("/comment/{id}/like")
    @SaCheckLogin
    public HttpResult likeComment(@PathVariable @Positive Long id) {
        log.info("收到点赞评论请求，commentId={}", id);
        userCommentService.likeComment(id);
        return HttpResult.success("点赞成功");
    }

    /**
     * 取消点赞评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @ApiOperation(value = "取消点赞评论")
    @DeleteMapping("/comment/{id}/like")
    @SaCheckLogin
    public HttpResult unlikeComment(@PathVariable @Positive Long id) {
        log.info("收到取消点赞评论请求，commentId={}", id);
        userCommentService.unlikeComment(id);
        return HttpResult.success("取消点赞成功");
    }

    /**
     * 我的评论列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "我的评论列表")
    @GetMapping("/my/comments")
    @SaCheckLogin
    public HttpResult myComments(@Validated MyCommentPageQueryDTO queryDTO) {
        log.info("收到我的评论列表请求，pageNum={}", queryDTO.getPageNum());
        MyCommentPageVO pageVO = userCommentService.pageMyComments(queryDTO);
        return HttpResult.success("查询成功", pageVO);
    }
}
