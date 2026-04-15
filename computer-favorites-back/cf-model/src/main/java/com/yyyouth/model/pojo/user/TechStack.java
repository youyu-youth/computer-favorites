package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-15
 *
 * 技术栈字典实体
 */
@Data
@TableName("t_tech_stack")
public class TechStack {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 技术栈名称
     */
    private String name;

    /**
     * PNG图标地址
     */
    @TableField("icon_png")
    private String iconPng;

    /**
     * 官网地址
     */
    @TableField("official_url")
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
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除，1已删除
     */
    private Integer deleted;
}
