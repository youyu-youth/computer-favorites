package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类树节点
 */
@Data
public class AdminCategoryTreeItemVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 状态：0禁用，1启用
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

    /**
     * 子节点
     */
    private List<AdminCategoryTreeItemVO> children = new ArrayList<>();
}
