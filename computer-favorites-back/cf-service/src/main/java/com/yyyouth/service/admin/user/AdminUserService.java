package com.yyyouth.service.admin.user;

import com.yyyouth.model.dto.admin.AdminUserPasswordResetDTO;
import com.yyyouth.model.dto.admin.AdminUserQueryDTO;
import com.yyyouth.model.dto.admin.AdminUserStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminUserDetailVO;
import com.yyyouth.model.vo.admin.AdminUserPageVO;
import com.yyyouth.model.vo.admin.AdminUserStatsVO;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    AdminUserPageVO queryUserPage(AdminUserQueryDTO queryDTO);

    /**
     * 查询用户详情（聚合基础信息+资料）
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    AdminUserDetailVO queryUserDetail(Long userId);

    /**
     * 查询用户统计数据
     *
     * @return 统计数据
     */
    AdminUserStatsVO queryUserStats();

    /**
     * 更新用户状态（禁用时同步踢出会话）
     *
     * @param userId    用户ID
     * @param updateDTO 状态参数
     */
    void updateUserStatus(Long userId, AdminUserStatusUpdateDTO updateDTO);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param resetDTO 密码参数
     */
    void resetUserPassword(Long userId, AdminUserPasswordResetDTO resetDTO);

    /**
     * 踢出用户所有在线会话
     *
     * @param userId 用户ID
     */
    void kickUserSessions(Long userId);
}
