package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈列表项
 */
@Data
public class AdminTechStackListItemVO {

    /**
     * 技术栈ID
     */
    private Long id;

    /**
     * 技术栈名称
     */
    private String name;

    /**
     * PNG图标地址
     */
    private String iconPng;

    /**
     * 官网地址
     */
    private String officialUrl;

    /**
     * 技术栈描述
     */
    private String description;

    /**
     * 主题色
     */
    private String color;

    /**
     * 状态：0禁用，1正常
     */
    private Integer status;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 关联用户数
     */
    private Long userCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
