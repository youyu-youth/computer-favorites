package com.yyyouth.web.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
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

    private static final List<String> DEFAULT_PERMISSIONS = List.of(
            "auth:session:renew",
            "auth:session:delete",
            "auth:session:detail"
    );

    private static final List<String> DEFAULT_ROLES = List.of("user");

    private final UserAccountMapper userAccountMapper;

    /**
     * 返回账号权限集合
     *
     * @param loginId 登录账号ID
     * @param loginType 登录体系
     * @return 权限集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (!isEnabledUser(loginId)) {
            return Collections.emptyList();
        }
        return DEFAULT_PERMISSIONS;
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
        if (!isEnabledUser(loginId)) {
            return Collections.emptyList();
        }
        return DEFAULT_ROLES;
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
}
