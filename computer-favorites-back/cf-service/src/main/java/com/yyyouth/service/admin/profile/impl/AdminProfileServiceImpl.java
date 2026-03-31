package com.yyyouth.service.admin.profile.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminProfileUpdateDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.vo.admin.AdminProfileVO;
import com.yyyouth.service.admin.profile.AdminProfileService;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员资料服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private static final int ENABLED_STATUS = 1;

    private static final int NOT_DELETED = 0;

    private final AdminAccountMapper adminAccountMapper;

    /**
     * 查询登录管理员资料
     *
     * @return 管理员资料
     */
    @Override
    public AdminProfileVO queryLoginAdminProfile() {
        AdminAccount currentAdmin = getCurrentActiveAdmin();
        return BeanUtil.copyProperties(currentAdmin, AdminProfileVO.class);
    }

    /**
     * 更新登录管理员资料
     *
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginAdminProfile(AdminProfileUpdateDTO updateDTO) {
        AdminAccount currentAdmin = getCurrentActiveAdmin();
        LambdaUpdateWrapper<AdminAccount> updateWrapper = new LambdaUpdateWrapper<AdminAccount>()
                .eq(AdminAccount::getId, currentAdmin.getId())
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .eq(AdminAccount::getStatus, ENABLED_STATUS);

        boolean hasUpdate = false;

        if (updateDTO.getNickname() != null) {
            updateWrapper.set(AdminAccount::getNickname, normalizeText(updateDTO.getNickname()));
            hasUpdate = true;
        }

        if (updateDTO.getEmail() != null) {
            String normalizedEmail = normalizeEmail(updateDTO.getEmail());
            if (!StringUtils.hasText(normalizedEmail)) {
                throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_FAILED.getCode(), "邮箱不能为空");
            }

            if (!normalizedEmail.equals(currentAdmin.getEmail())) {
                AdminAccount duplicateAdmin = adminAccountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                        .eq(AdminAccount::getDeleted, NOT_DELETED)
                        .eq(AdminAccount::getEmail, normalizedEmail)
                        .ne(AdminAccount::getId, currentAdmin.getId())
                        .last("limit 1"));
                if (duplicateAdmin != null) {
                    throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS.getCode(), AuthErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
                }
                updateWrapper.set(AdminAccount::getEmail, normalizedEmail);
                hasUpdate = true;
            }
        }

        if (!hasUpdate) {
            return;
        }

        updateWrapper.set(AdminAccount::getUpdateTime, LocalDateTime.now());
        int affectedRows = adminAccountMapper.update(null, updateWrapper);
        if (affectedRows != 1) {
            throw new BusinessException(AuthErrorCode.ADMIN_PROFILE_UPDATE_FAILED.getCode(), AuthErrorCode.ADMIN_PROFILE_UPDATE_FAILED.getMessage());
        }
    }

    /**
     * 查询当前登录且有效的管理员账号
     *
     * @return 管理员账号
     */
    private AdminAccount getCurrentActiveAdmin() {
        StpAdminUtil.checkLogin();
        Long adminId = StpAdminUtil.getLoginIdAsLong();
        AdminAccount adminAccount = adminAccountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getId, adminId)
                .eq(AdminAccount::getDeleted, NOT_DELETED)
                .eq(AdminAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (adminAccount == null) {
            throw new BusinessException(AuthErrorCode.ADMIN_PROFILE_NOT_FOUND.getCode(), AuthErrorCode.ADMIN_PROFILE_NOT_FOUND.getMessage());
        }
        return adminAccount;
    }

    /**
     * 归一化文本
     *
     * @param value 原始值
     * @return 归一化结果
     */
    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 归一化邮箱
     *
     * @param email 原始邮箱
     * @return 归一化邮箱
     */
    private String normalizeEmail(String email) {
        String normalizedEmail = normalizeText(email);
        if (!StringUtils.hasText(normalizedEmail)) {
            return null;
        }
        return normalizedEmail.toLowerCase(Locale.ROOT);
    }
}