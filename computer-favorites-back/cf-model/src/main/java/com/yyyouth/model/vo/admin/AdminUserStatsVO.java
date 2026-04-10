package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户统计数据
 */
@Data
public class AdminUserStatsVO {

    /**
     * 总用户数（未删除）
     */
    private Long total;

    /**
     * 正常状态用户数
     */
    private Long normal;

    /**
     * 禁用用户数
     */
    private Long disabled;

    /**
     * 邮箱已验证用户数
     */
    private Long emailVerified;
}
