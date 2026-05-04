package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 发布评论参数
 */
@Data
public class CommentCreateDTO {

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /**
     * 父评论ID，为空或0表示顶级评论
     */
    @Positive(message = "父评论ID不合法")
    private Long parentId;

    /**
     * 回复用户ID
     */
    @Positive(message = "回复用户ID不合法")
    private Long replyUserId;
}
