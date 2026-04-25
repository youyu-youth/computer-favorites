package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-23
 *
 * 用户公告详情
 */
@Data
public class UserAnnouncementDetailVO {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型：1-新增内容，2-Bug修复，3-系统更新
     */
    private Integer type;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
