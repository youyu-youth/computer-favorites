package com.yyyouth.web.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.CommonConstants;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * Sa-Token权限加载实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private static final int ENABLED_STATUS = 1;

    private static final int NOT_DELETED = 0;

    private static final List<String> DEFAULT_USER_PERMISSIONS = List.of(
            "auth:session:renew",
            "auth:session:delete",
            "auth:session:detail",
            "user:website:submit",
            "user:website:list",
            "user:website:detail",
            "user:website:edit",
            "user:website:cancel",
            "user:website:resubmit"
    );

    private static final List<String> DEFAULT_ADMIN_PERMISSIONS = List.of(
            "admin:auth:renew",
            "admin:auth:delete",
            "admin:auth:detail",
            "admin:profile:detail",
            "admin:profile:edit",
            "admin:profile:password",
            "admin:website:list",
            "admin:website:detail",
            "admin:website:edit",
            "admin:website:category:list",
            "admin:website:stats",
            "admin:website:add",
            "admin:website:status",
            "admin:website:audit",
            "admin:website:delete",
            "admin:category:list",
            "admin:category:add",
            "admin:category:edit",
            "admin:category:delete",
            "admin:tag:list",
            "admin:tag:stats",
            "admin:tag:add",
            "admin:tag:edit",
            "admin:tag:delete"
    );

    private static final List<String> DEFAULT_USER_ROLES = List.of("user");

    private static final List<String> DEFAULT_ADMIN_ROLES = List.of(CommonConstants.SUPER_ADMIN);

    private static final String ADMIN_LOGIN_TYPE = "admin";

    private final UserAccountMapper userAccountMapper;

    private final AdminAccountMapper adminAccountMapper;

    /**
     * 返回账号权限集合
     *
     * @param loginId 登录账号ID
     * @param loginType 登录体系
     * @return 权限集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (isAdminLoginType(loginType)) {
            if (!isEnabledAdmin(loginId)) {
                return Collections.emptyList();
            }
            return DEFAULT_ADMIN_PERMISSIONS;
        }
        if (!isEnabledUser(loginId)) {
            return Collections.emptyList();
        }
        return DEFAULT_USER_PERMISSIONS;
    }

    /**
     * 返回账号角色集合
     *
     * @param loginId 登录账号ID
     * @param loginType 登录体系
     * @return 角色集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (isAdminLoginType(loginType)) {
            if (!isEnabledAdmin(loginId)) {
                return Collections.emptyList();
            }
            return DEFAULT_ADMIN_ROLES;
        }
        if (!isEnabledUser(loginId)) {
            return Collections.emptyList();
        }
        return DEFAULT_USER_ROLES;
    }

    /**
     * 判断是否为管理员登录体系
     *
     * @param loginType 登录体系
     * @return 是否管理员登录体系
     */
    private boolean isAdminLoginType(String loginType) {
        return ADMIN_LOGIN_TYPE.equals(loginType);
    }

    /**
     * 判断账号是否启用
     *
     * @param loginId 登录账号ID
     * @return 是否启用
     */
    private boolean isEnabledUser(Object loginId) {
        Long userId;
        try {
            userId = Long.parseLong(String.valueOf(loginId));
        } catch (Exception ex) {
            return false;
        }
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .last("limit 1"));
        return userAccount != null;
    }

    /**
     * 判断管理员账号是否启用
     *
     * @param loginId 登录账号ID
     * @return 是否启用
     */
    private boolean isEnabledAdmin(Object loginId) {
        Long adminId;
        try {
            adminId = Long.parseLong(String.valueOf(loginId));
        } catch (Exception ex) {
            return false;
        }
        AdminAccount adminAccount = adminAccountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getId, adminId)
                .eq(AdminAccount::getStatus, ENABLED_STATUS)
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .last("limit 1"));
        return adminAccount != null;
    }
}
