package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿详情
 */
@Data
public class UserWebsiteSubmissionDetailVO {

    /**
     * 网站ID
     */
    private Long id;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站URL
     */
    private String url;

    /**
     * 图标地址
     */
    private String icon;

    /**
     * Github 地址
     */
    private String githubUrl;

    /**
     * 网站简介
     */
    private String summary;

    /**
     * 网站描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签
     */
    private List<UserWebsiteTagItemVO> tags;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 上下架状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
