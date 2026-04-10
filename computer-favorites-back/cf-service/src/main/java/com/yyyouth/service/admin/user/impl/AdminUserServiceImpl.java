package com.yyyouth.service.admin.user.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyyouth.model.dto.admin.AdminUserPasswordResetDTO;
import com.yyyouth.model.dto.admin.AdminUserQueryDTO;
import com.yyyouth.model.dto.admin.AdminUserStatusUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.vo.admin.AdminUserDetailVO;
import com.yyyouth.model.vo.admin.AdminUserListItemVO;
import com.yyyouth.model.vo.admin.AdminUserPageVO;
import com.yyyouth.model.vo.admin.AdminUserStatsVO;
import com.yyyouth.service.admin.user.AdminUserService;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final int ENABLED_STATUS = 1;
    private static final int DISABLED_STATUS = 0;

    @Override
    public AdminUserPageVO queryUserPage(AdminUserQueryDTO queryDTO) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getDeleted, 0);

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserAccount::getStatus, queryDTO.getStatus());
        }

        // 关键字模糊搜索（用户名/邮箱/昵称）
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w
                    .like(UserAccount::getUsername, kw)
                    .or().like(UserAccount::getEmail, kw)
                    .or().like(UserAccount::getNickname, kw));
        }

        // 按最近登录时间倒序
        wrapper.orderByDesc(UserAccount::getLastLoginTime);

        Page<UserAccount> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<UserAccount> result = userAccountMapper.selectPage(page, wrapper);

        List<AdminUserListItemVO> records = result.getRecords().stream()
                .map(this::toListItemVO)
                .collect(Collectors.toList());

        AdminUserPageVO vo = new AdminUserPageVO();
        vo.setRecords(records);
        vo.setTotal(result.getTotal());
        vo.setPageNum(queryDTO.getPageNum());
        vo.setPageSize(queryDTO.getPageSize());
        vo.setTotalPages(result.getPages());
        return vo;
    }

    @Override
    public AdminUserDetailVO queryUserDetail(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        AdminUserDetailVO vo = new AdminUserDetailVO();
        // 基础信息
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setEmailVerified(user.getEmailVerified());
        vo.setPhoneVerified(user.getPhoneVerified());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());

        // 用户资料（可能不存在）
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .eq(UserProfile::getDeleted, 0));
        if (profile != null) {
            vo.setGender(profile.getGender());
            vo.setCity(profile.getCity());
            vo.setSignature(profile.getSignature());
            vo.setTechStack(profile.getTechStack());
            vo.setGithubUrl(profile.getGithubUrl());
            vo.setBlogUrl(profile.getBlogUrl());
        }

        return vo;
    }

    @Override
    public AdminUserStatsVO queryUserStats() {
        long total = userAccountMapper.selectCount(
                new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getDeleted, 0));
        long normal = userAccountMapper.selectCount(
                new LambdaQueryWrapper<UserAccount>()
                        .eq(UserAccount::getDeleted, 0)
                        .eq(UserAccount::getStatus, ENABLED_STATUS));
        long disabled = userAccountMapper.selectCount(
                new LambdaQueryWrapper<UserAccount>()
                        .eq(UserAccount::getDeleted, 0)
                        .eq(UserAccount::getStatus, DISABLED_STATUS));
        long emailVerified = userAccountMapper.selectCount(
                new LambdaQueryWrapper<UserAccount>()
                        .eq(UserAccount::getDeleted, 0)
                        .eq(UserAccount::getEmailVerified, 1));

        AdminUserStatsVO vo = new AdminUserStatsVO();
        vo.setTotal(total);
        vo.setNormal(normal);
        vo.setDisabled(disabled);
        vo.setEmailVerified(emailVerified);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, AdminUserStatusUpdateDTO updateDTO) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        UserAccount update = new UserAccount();
        update.setId(userId);
        update.setStatus(updateDTO.getStatus());
        userAccountMapper.updateById(update);

        // 禁用时主动踢出所有在线会话
        if (DISABLED_STATUS == updateDTO.getStatus()) {
            log.info("用户 [{}] 被禁用，踢出所有会话", userId);
            try {
                StpUtil.logout(userId);
            } catch (Exception e) {
                log.warn("踢出用户 [{}] 会话失败: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId, AdminUserPasswordResetDTO resetDTO) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        String newPasswordHash = PASSWORD_ENCODER.encode(resetDTO.getNewPassword());

        UserAccount update = new UserAccount();
        update.setId(userId);
        update.setPasswordHash(newPasswordHash);
        userAccountMapper.updateById(update);

        log.info("用户 [{}] 密码已由管理员重置", userId);
    }

    @Override
    public void kickUserSessions(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }
        log.info("踢出用户 [{}] 所有在线会话", userId);
        StpUtil.logout(userId);
    }

    /**
     * 转换为列表项 VO
     */
    private AdminUserListItemVO toListItemVO(UserAccount user) {
        AdminUserListItemVO vo = new AdminUserListItemVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setEmailVerified(user.getEmailVerified());
        vo.setPhoneVerified(user.getPhoneVerified());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setDeleted(user.getDeleted());
        return vo;
    }
}
