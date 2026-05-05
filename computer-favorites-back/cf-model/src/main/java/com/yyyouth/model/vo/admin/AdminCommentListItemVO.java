package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论列表项
 */
@Data
public class AdminCommentListItemVO {

    private Long id;

    private Long websiteId;

    private String websiteName;

    private String websiteIcon;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Integer reportCount;

    /**
     * 状态：0隐藏，1显示
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
