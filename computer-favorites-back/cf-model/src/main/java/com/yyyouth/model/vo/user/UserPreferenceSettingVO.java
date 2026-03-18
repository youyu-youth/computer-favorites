package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户偏好设置视图
 */
@Data
public class UserPreferenceSettingVO {

    /**
     * 主题
     */
    private String theme;

    /**
     * 语言
     */
    private String language;

    /**
     * 邮件通知
     */
    private Integer emailNotice;

    /**
     * 收藏通知
     */
    private Integer collectNotice;

    /**
     * 评论通知
     */
    private Integer commentNotice;

    /**
     * 主页样式
     */
    private String homepageStyle;

    /**
     * 每页条数
     */
    private Integer pageSize;
}
