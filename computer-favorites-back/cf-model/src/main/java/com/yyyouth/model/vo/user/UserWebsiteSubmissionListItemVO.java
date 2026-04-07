package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿列表项
 */
@Data
public class UserWebsiteSubmissionListItemVO {

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
     * 网站图标
     */
    private String icon;

    /**
     * 网站简介
     */
    private String summary;

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
     * 更新时间
     */
    private LocalDateTime updateTime;
}
