package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈统计
 */
@Data
public class AdminTechStackStatsVO {

    /**
     * 技术栈总数
     */
    private Long total;

    /**
     * 已启用数量
     */
    private Long enabled;

    /**
     * 已禁用数量
     */
    private Long disabled;
}
