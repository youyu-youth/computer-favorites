package com.yyyouth.service.user.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.LoginUserProfileQueryDTO;
import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.TechStack;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.model.vo.user.UserBasicInfoVO;
import com.yyyouth.model.vo.user.UserDetailProfileVO;
import com.yyyouth.model.vo.user.UserPreferenceSettingVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.user.TechStackMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final String AVATAR_BUSINESS_PATH = "user-avatar";

    private final UserAccountMapper userAccountMapper;

    private final UserProfileMapper userProfileMapper;

    private final UserSettingMapper userSettingMapper;

    private final TechStackMapper techStackMapper;

    private final MinioFileService minioFileService;

    /**
     * 聚合查询登录用户资料
     *
     * @return 登录用户资料
     */
    @Override
    public LoginUserProfileVO queryLoginUserProfile() {
        StpUtil.checkLogin();
        LoginUserProfileQueryDTO queryDTO = new LoginUserProfileQueryDTO();
        queryDTO.setUserId(StpUtil.getLoginIdAsLong());

        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, queryDTO.getUserId())
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }

        UserProfile userProfile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, queryDTO.getUserId())
                .eq(UserProfile::getDeleted, 0)
                .last("limit 1"));

        UserSetting userSetting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, queryDTO.getUserId())
                .last("limit 1"));

        LoginUserProfileVO resultVO = new LoginUserProfileVO();
        resultVO.setUser(copyOrNull(userAccount, UserBasicInfoVO.class));
        UserDetailProfileVO profileVO = copyOrNull(userProfile, UserDetailProfileVO.class);
        if (profileVO != null && userProfile != null) {
            profileVO.setTechStack(resolveTechStackNames(userProfile.getTechStack()));
        }
        resultVO.setProfile(profileVO);
        resultVO.setSetting(copyOrNull(userSetting, UserPreferenceSettingVO.class));
        return resultVO;
    }

    /**
     * 更新登录用户资料
     *
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginUserProfile(UserProfileUpdateDTO updateDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }

        if (updateDTO.getNickname() != null) {
            userAccount.setNickname(normalizeText(updateDTO.getNickname()));
            userAccountMapper.updateById(userAccount);
        }

        UserProfile userProfile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .eq(UserProfile::getDeleted, 0)
                .last("limit 1"));

        boolean shouldInsert = userProfile == null;
        if (shouldInsert) {
            userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setDeleted(0);
        }

        if (updateDTO.getGender() != null) {
            userProfile.setGender(updateDTO.getGender());
        }
        if (updateDTO.getCountry() != null) {
            userProfile.setCountry(normalizeText(updateDTO.getCountry()));
        }
        if (updateDTO.getCity() != null) {
            userProfile.setCity(normalizeText(updateDTO.getCity()));
        }
        if (updateDTO.getGithubUrl() != null) {
            userProfile.setGithubUrl(normalizeText(updateDTO.getGithubUrl()));
        }
        if (updateDTO.getGiteeUrl() != null) {
            userProfile.setGiteeUrl(normalizeText(updateDTO.getGiteeUrl()));
        }
        if (updateDTO.getOtherRepoLinks() != null) {
            userProfile.setOtherRepoLinks(normalizeText(updateDTO.getOtherRepoLinks()));
        }
        if (updateDTO.getBlogUrl() != null) {
            userProfile.setBlogUrl(normalizeText(updateDTO.getBlogUrl()));
        }
        if (updateDTO.getSignature() != null) {
            userProfile.setSignature(normalizeText(updateDTO.getSignature()));
        }
        if (updateDTO.getHobbyTags() != null) {
            userProfile.setHobbyTags(normalizeCsv(updateDTO.getHobbyTags()));
        }
        if (updateDTO.getTechStack() != null) {
            userProfile.setTechStack(normalizeCsv(updateDTO.getTechStack()));
        }

        if (shouldInsert) {
            userProfileMapper.insert(userProfile);
            return;
        }
        userProfileMapper.updateById(userProfile);
    }

    /**
     * 上传登录用户头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAvatarUploadVO uploadLoginUserAvatar(MultipartFile file) {
        StpUtil.checkLogin();
        UserAccount userAccount = getCurrentActiveUser();

        String oldAvatarUrl = userAccount.getAvatar();
        UserAvatarUploadVO uploadVO = minioFileService.uploadAvatar(file, AVATAR_BUSINESS_PATH);

        userAccount.setAvatar(uploadVO.getAvatarUrl());
        userAccountMapper.updateById(userAccount);

        if (StringUtils.hasText(oldAvatarUrl) && !oldAvatarUrl.equals(uploadVO.getAvatarUrl())) {
            minioFileService.deleteByUrl(oldAvatarUrl);
        }
        return uploadVO;
    }

    /**
     * 删除登录用户头像
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoginUserAvatar() {
        StpUtil.checkLogin();
        UserAccount userAccount = getCurrentActiveUser();

        String oldAvatarUrl = userAccount.getAvatar();
        if (!StringUtils.hasText(oldAvatarUrl)) {
            return;
        }

        minioFileService.deleteByUrl(oldAvatarUrl);
        userAccount.setAvatar("");
        userAccountMapper.updateById(userAccount);
    }

    private String resolveTechStackNames(String techStackRaw) {
        if (ObjectUtils.isEmpty(techStackRaw)) {
            return "";
        }

        List<String> tokens = splitCsvTokens(techStackRaw);
        if (tokens.isEmpty()) {
            return "";
        }

        List<Long> techStackIds = tokens.stream()
                .filter(item -> item.matches("\\d+"))
                .map(Long::valueOf)
                .distinct()
                .collect(Collectors.toList());

        if (techStackIds.isEmpty()) {
            return String.join(",", tokens);
        }

        List<TechStack> techStacks = techStackMapper.selectList(new LambdaQueryWrapper<TechStack>()
                .in(TechStack::getId, techStackIds)
                .eq(TechStack::getStatus, 1)
                .eq(TechStack::getDeleted, 0));

        Map<Long, String> idNameMap = techStacks.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(TechStack::getId, TechStack::getName, (left, right) -> left));

        return tokens.stream()
                .map(token -> token.matches("\\d+") ? idNameMap.getOrDefault(Long.valueOf(token), token) : token)
                .collect(Collectors.joining(","));
    }

    private List<Long> parseTechStackIds(String techStackIdsRaw) {
        if (ObjectUtils.isEmpty(techStackIdsRaw)) {
            return Collections.emptyList();
        }
        return Arrays.stream(techStackIdsRaw.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .filter(item -> item.matches("\\d+"))
                .map(Long::valueOf)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> splitCsvTokens(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split("[，,]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private String normalizeCsv(String value) {
        if (value == null) {
            return null;
        }
        Set<String> distinctOrdered = new LinkedHashSet<>(splitCsvTokens(value));
        return String.join(",", distinctOrdered);
    }

    /**
     * 获取当前登录可用用户
     *
     * @return 用户账号
     */
    private UserAccount getCurrentActiveUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        return userAccount;
    }

    /**
     * 复制对象属性，空对象直接返回空
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @return 复制结果
     * @param <T> 目标泛型
     */
    private <T> T copyOrNull(Object source, Class<T> targetClass) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }
}
