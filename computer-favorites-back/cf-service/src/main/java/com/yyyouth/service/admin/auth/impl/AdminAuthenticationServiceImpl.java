package com.yyyouth.service.admin.auth.impl;

import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthUserVO;
import com.yyyouth.service.admin.auth.AdminAuthenticationService;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthenticationServiceImpl implements AdminAuthenticationService {

    private static final int ENABLED_STATUS = 1;

    private static final int NOT_DELETED = 0;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AdminAccountMapper adminAccountMapper;

    /**
     * 管理员登录并建立会话
     *
     * @param loginDTO 登录参数
     * @param loginIp 登录IP
     * @return 登录结果
     */
    @Override
    public AuthLoginVO login(AuthLoginDTO loginDTO, String loginIp) {
        String normalizedUsername = normalizeUsername(loginDTO.getUsername());
        AdminAccount adminAccount = adminAccountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .eq(AdminAccount::getUsername, normalizedUsername)
                .last("limit 1"));
        if (adminAccount == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(adminAccount.getStatus())) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }

        String encodedPassword = resolveEncodedPassword(adminAccount);
        if (!StringUtils.hasText(encodedPassword) || !PASSWORD_ENCODER.matches(loginDTO.getPassword(), encodedPassword)) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIAL.getCode(), AuthErrorCode.INVALID_CREDENTIAL.getMessage());
        }

        return doLogin(adminAccount, loginDTO.getDeviceType(), loginIp);
    }

    /**
     * 执行登录并构建返回结果
     */
    private AuthLoginVO doLogin(AdminAccount adminAccount, String deviceType, String loginIp) {
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(deviceType);
        StpAdminUtil.login(adminAccount.getId(), loginParameter);
        StpAdminUtil.getTokenSession().set("deviceType", deviceType);

        String tokenValue = StpAdminUtil.getTokenValue();
        long timeoutSeconds = StpAdminUtil.getTokenTimeout();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = calculateExpireTime(timeoutSeconds, now);

        updateLoginAudit(adminAccount.getId(), loginIp, now);

        AuthUserVO authUserVO = BeanUtil.copyProperties(adminAccount, AuthUserVO.class);
        authUserVO.setUserId(adminAccount.getId());

        AuthLoginVO authLoginVO = new AuthLoginVO();
        authLoginVO.setTokenValue(tokenValue);
        authLoginVO.setTokenName(StpAdminUtil.getTokenName());
        authLoginVO.setExpireTime(expireTime);
        authLoginVO.setUserInfo(authUserVO);
        return authLoginVO;
    }

    /**
     * 更新管理员登录审计信息
     */
    private void updateLoginAudit(Long adminId, String loginIp, LocalDateTime now) {
        adminAccountMapper.update(null, new LambdaUpdateWrapper<AdminAccount>()
                .eq(AdminAccount::getId, adminId)
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .set(AdminAccount::getLastLoginTime, now)
                .set(AdminAccount::getLastLoginIp, normalizeLoginIp(loginIp))
                .set(AdminAccount::getUpdateTime, now));
    }

    /**
     * 计算过期时间
     */
    private LocalDateTime calculateExpireTime(long timeoutSeconds, LocalDateTime baseTime) {
        if (timeoutSeconds <= 0) {
            return null;
        }
        return baseTime.plusSeconds(timeoutSeconds);
    }

    /**
     * 解析数据库中的加密密码
     */
    private String resolveEncodedPassword(AdminAccount adminAccount) {
        if (StringUtils.hasText(adminAccount.getPasswordHash())) {
            return adminAccount.getPasswordHash();
        }
        return adminAccount.getPassword();
    }

    /**
     * 规范化用户名
     */
    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化登录IP
     */
    private String normalizeLoginIp(String loginIp) {
        if (!StringUtils.hasText(loginIp)) {
            return null;
        }
        return loginIp.trim();
    }
}
