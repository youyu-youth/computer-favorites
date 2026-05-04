package com.yyyouth.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 我的评论条目视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCommentItemVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 网站ID
     */
    private Long websiteId;

    /**
     * 网站名称
     */
    private String websiteName;

    /**
     * 网站图标
     */
    private String websiteIcon;

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
     * 父评论ID
     */
    private Long parentId;

    /**
     * 评论是否已删除
     */
    private Boolean isDeleted;
}
