package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告列表项
 */
@Data
public class AdminAnnouncementListItemVO {

    /**
     * 主键ID
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
     * 类型：1-新增内容，2-Bug修复，3-更新系统
     */
    private Integer type;

    /**
     * 是否置顶：0否，1是
     */
    private Integer isTop;

    /**
     * 状态：0隐藏，1显示
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
