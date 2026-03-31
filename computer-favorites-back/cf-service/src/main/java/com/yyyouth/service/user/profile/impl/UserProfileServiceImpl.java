package com.yyyouth.service.user.profile.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.CaptchaConstants;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.EmailUtils;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.user.UserEmailCodeSendDTO;
import com.yyyouth.model.dto.user.UserEmailUpdateDTO;
import com.yyyouth.model.dto.user.UserPreferenceSettingUpdateDTO;
import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.dto.user.UserUsernameUpdateDTO;
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
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.user.TechStackMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.user.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    private static final int USERNAME_MAX_LENGTH = 120;

    private static final int VERIFIED = 1;

    private static final int NOT_DELETED = 0;

    private static final int ENABLED_STATUS = 1;

    private final UserAccountMapper userAccountMapper;

    private final UserProfileMapper userProfileMapper;

    private final UserSettingMapper userSettingMapper;

    private final TechStackMapper techStackMapper;

    private final MinioFileService minioFileService;

    private final StringRedisTemplate stringRedisTemplate;

    private final EmailUtils emailUtils;

    /**
     * 聚合查询登录用户资料
     *
     * @return 登录用户资料
     */
    @Override
    public LoginUserProfileVO queryLoginUserProfile() {
        StpUtil.checkLogin();
        UserAccount userAccount = getCurrentActiveUser();
        Long userId = userAccount.getId();

        UserProfile userProfile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .eq(UserProfile::getDeleted, 0)
                .last("limit 1"));

        UserSetting userSetting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, userId)
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
        UserAccount userAccount = getCurrentActiveUser();
        Long userId = userAccount.getId();

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
     * 修改登录用户用户名
     *
     * @param updateDTO 用户名修改参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginUsername(UserUsernameUpdateDTO updateDTO) {
        StpUtil.checkLogin();
        UserAccount currentUser = getCurrentActiveUserWithUsernameUpdateTime();
        String normalizedUsername = normalizeText(updateDTO.getUsername());
        if (!StringUtils.hasText(normalizedUsername) || normalizedUsername.length() > USERNAME_MAX_LENGTH) {
            throw new BusinessException(AuthErrorCode.USERNAME_UPDATE_FAILED.getCode(), "用户名长度需在1-120之间");
        }
        if (normalizedUsername.equals(currentUser.getUsername())) {
            throw new BusinessException(AuthErrorCode.USERNAME_SAME_AS_OLD.getCode(), AuthErrorCode.USERNAME_SAME_AS_OLD.getMessage());
        }
        if (SensitiveWordUtils.containsForUserContent(normalizedUsername)) {
            throw new BusinessException(AuthErrorCode.USERNAME_CONTAINS_SENSITIVE_WORD.getCode(), AuthErrorCode.USERNAME_CONTAINS_SENSITIVE_WORD.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        if (isSameMonth(currentUser.getUsernameUpdateTime(), now)) {
            throw new BusinessException(AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getCode(), AuthErrorCode.USERNAME_UPDATE_MONTHLY_LIMIT.getMessage());
        }

        UserAccount duplicateUser = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, 0)
                .eq(UserAccount::getUsername, normalizedUsername)
                .ne(UserAccount::getId, currentUser.getId())
                .last("limit 1"));
        if (duplicateUser != null) {
            throw new BusinessException(AuthErrorCode.USERNAME_ALREADY_EXISTS.getCode(), AuthErrorCode.USERNAME_ALREADY_EXISTS.getMessage());
        }

        try {
            int updatedRows = userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                    .eq(UserAccount::getId, currentUser.getId())
                    .eq(UserAccount::getDeleted, 0)
                    .set(UserAccount::getUsername, normalizedUsername)
                    .set(UserAccount::getUsernameUpdateTime, now)
                    .set(UserAccount::getUpdateTime, now));
            if (updatedRows != 1) {
                throw new BusinessException(AuthErrorCode.USERNAME_UPDATE_FAILED.getCode(), AuthErrorCode.USERNAME_UPDATE_FAILED.getMessage());
            }
            log.info("用户名修改成功，userId={}", currentUser.getId());
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(AuthErrorCode.USERNAME_ALREADY_EXISTS.getCode(), AuthErrorCode.USERNAME_ALREADY_EXISTS.getMessage());
        }
    }

    /**
     * 更新登录用户偏好设置
     *
     * @param updateDTO 偏好设置参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginUserSetting(UserPreferenceSettingUpdateDTO updateDTO) {
        StpUtil.checkLogin();
        UserAccount currentUser = getCurrentActiveUser();

        UserSetting userSetting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, currentUser.getId())
                .last("limit 1"));

        boolean shouldInsert = userSetting == null;
        if (shouldInsert) {
            userSetting = new UserSetting();
            userSetting.setUserId(currentUser.getId());
            userSetting.setCreateTime(LocalDateTime.now());
        }

        userSetting.setTheme(normalizeTheme(updateDTO.getTheme()));
        userSetting.setLanguage(normalizeLanguage(updateDTO.getLanguage()));
        userSetting.setEmailNotice(updateDTO.getEmailNotice());
        userSetting.setCollectNotice(updateDTO.getCollectNotice());
        userSetting.setCommentNotice(updateDTO.getCommentNotice());
        userSetting.setHomepageStyle(normalizeHomepageStyle(updateDTO.getHomepageStyle()));
        userSetting.setPageSize(updateDTO.getPageSize());
        userSetting.setUpdateTime(LocalDateTime.now());

        int affectedRows = shouldInsert ? userSettingMapper.insert(userSetting) : userSettingMapper.updateById(userSetting);
        if (affectedRows != 1) {
            throw new BusinessException(AuthErrorCode.USER_SETTING_UPDATE_FAILED.getCode(), AuthErrorCode.USER_SETTING_UPDATE_FAILED.getMessage());
        }
    }

    /**
     * 发送邮箱修改验证码
     *
     * @param sendDTO 发送参数
     */
    @Override
    public void sendEmailUpdateCode(UserEmailCodeSendDTO sendDTO) {
        StpUtil.checkLogin();
        UserAccount currentUser = getCurrentActiveUser();
        String normalizedEmail = normalizeEmail(sendDTO.getEmail());
        validateEmailUpdateTarget(currentUser, normalizedEmail);

        enforceEmailUpdateCodeSendPolicy(normalizedEmail);
        String code = cn.hutool.core.util.RandomUtil.randomNumbers(6);
        boolean sendSuccess;
        try {
            sendSuccess = emailUtils.sendGeneralEmail(
                    CaptchaConstants.CAPTCHA_TITLE,
                    "您的邮箱修改验证码为：" + code + "，" + CaptchaConstants.CODE_EXPIRE_MINUTES + "分钟内有效。",
                    normalizedEmail
            );
        } catch (Exception ex) {
            log.error("发送邮箱修改验证码失败，userId={}, email={}", currentUser.getId(), normalizedEmail, ex);
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_SEND_FAILED.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_SEND_FAILED.getMessage());
        }
        if (!sendSuccess) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_SEND_FAILED.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_SEND_FAILED.getMessage());
        }

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(
                buildEmailUpdateCodeKey(normalizedEmail),
                code,
                CaptchaConstants.CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * 校验邮箱修改验证码
     *
     * @param updateDTO 校验参数
     */
    @Override
    public void verifyEmailUpdateCode(UserEmailUpdateDTO updateDTO) {
        StpUtil.checkLogin();
        UserAccount currentUser = getCurrentActiveUser();
        String normalizedEmail = normalizeEmail(updateDTO.getEmail());
        validateEmailUpdateTarget(currentUser, normalizedEmail);
        validateEmailUpdateCode(normalizedEmail, updateDTO.getEmailCode());
    }

    /**
     * 修改登录用户邮箱
     *
     * @param updateDTO 邮箱修改参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginEmail(UserEmailUpdateDTO updateDTO) {
        StpUtil.checkLogin();
        UserAccount currentUser = getCurrentActiveUser();
        String normalizedEmail = normalizeEmail(updateDTO.getEmail());
        validateEmailUpdateTarget(currentUser, normalizedEmail);
        validateEmailUpdateCode(normalizedEmail, updateDTO.getEmailCode());

        int updatedRows = userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                .eq(UserAccount::getId, currentUser.getId())
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .set(UserAccount::getEmail, normalizedEmail)
                .set(UserAccount::getEmailVerified, VERIFIED)
                .set(UserAccount::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_FAILED.getCode(), AuthErrorCode.EMAIL_UPDATE_FAILED.getMessage());
        }

        stringRedisTemplate.delete(buildEmailUpdateCodeKey(normalizedEmail));
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

    /**
     * 校验目标邮箱是否合法且可用于当前用户修改
     *
     * @param currentUser 当前用户
     * @param normalizedEmail 目标邮箱
     */
    private void validateEmailUpdateTarget(UserAccount currentUser, String normalizedEmail) {
        String currentEmail = normalizeEmail(currentUser.getEmail());
        if (normalizedEmail.equals(currentEmail)) {
            throw new BusinessException(AuthErrorCode.EMAIL_SAME_AS_OLD.getCode(), AuthErrorCode.EMAIL_SAME_AS_OLD.getMessage());
        }

        UserAccount duplicateUser = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getEmail, normalizedEmail)
                .ne(UserAccount::getId, currentUser.getId())
                .last("limit 1"));
        if (duplicateUser != null) {
            throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS.getCode(), AuthErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
        }
    }

    /**
     * 校验邮箱修改验证码
     *
     * @param email 邮箱
     * @param code 验证码
     */
    private void validateEmailUpdateCode(String email, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(buildEmailUpdateCodeKey(email));
        if (!StringUtils.hasText(cacheCode)) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_EXPIRED.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_EXPIRED.getMessage());
        }
        if (!cacheCode.equals(code)) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_INVALID.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_INVALID.getMessage());
        }
    }

    /**
     * 校验邮箱修改验证码发送策略
     *
     * @param email 邮箱
     */
    private void enforceEmailUpdateCodeSendPolicy(String email) {
        String intervalKey = buildEmailUpdateCodeIntervalKey(email);
        Boolean exists = stringRedisTemplate.hasKey(intervalKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_SEND_TOO_FAST.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_SEND_TOO_FAST.getMessage());
        }

        String countKey = buildEmailUpdateCodeCountKey(email);
        Integer sendCount = toInteger(stringRedisTemplate.opsForValue().get(countKey));
        if (sendCount != null && sendCount >= CaptchaConstants.MAX_SEND_COUNT_PER_DAY) {
            throw new BusinessException(AuthErrorCode.EMAIL_UPDATE_CODE_SEND_LIMIT.getCode(), AuthErrorCode.EMAIL_UPDATE_CODE_SEND_LIMIT.getMessage());
        }

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.increment(countKey);
        stringRedisTemplate.expire(countKey, 1, TimeUnit.DAYS);
        valueOperations.set(intervalKey, "1", CaptchaConstants.SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 构建邮箱修改验证码键
     *
     * @param email 邮箱
     * @return Redis 键
     */
    private String buildEmailUpdateCodeKey(String email) {
        return CaptchaConstants.EMAIL_UPDATE_CODE_KEY_PREFIX + email;
    }

    /**
     * 构建邮箱修改验证码发送间隔键
     *
     * @param email 邮箱
     * @return Redis 键
     */
    private String buildEmailUpdateCodeIntervalKey(String email) {
        return buildEmailUpdateCodeKey(email) + ":interval";
    }

    /**
     * 构建邮箱修改验证码发送计数键
     *
     * @param email 邮箱
     * @return Redis 键
     */
    private String buildEmailUpdateCodeCountKey(String email) {
        return buildEmailUpdateCodeKey(email) + ":count:" + LocalDate.now();
    }

    /**
     * 规范化邮箱
     *
     * @param email 原始邮箱
     * @return 规范化邮箱
     */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化主题模式
     *
     * @param theme 原始主题
     * @return 规范化主题
     */
    private String normalizeTheme(String theme) {
        String normalized = normalizeText(theme);
        if (!"light".equals(normalized) && !"dark".equals(normalized) && !"system".equals(normalized)) {
            throw new BusinessException(AuthErrorCode.USER_SETTING_INVALID_PARAM.getCode(), AuthErrorCode.USER_SETTING_INVALID_PARAM.getMessage());
        }
        return normalized;
    }

    /**
     * 规范化语言
     *
     * @param language 原始语言
     * @return 规范化语言
     */
    private String normalizeLanguage(String language) {
        String normalized = normalizeText(language);
        if (!"zh-CN".equals(normalized) && !"en-US".equals(normalized)) {
            throw new BusinessException(AuthErrorCode.USER_SETTING_INVALID_PARAM.getCode(), AuthErrorCode.USER_SETTING_INVALID_PARAM.getMessage());
        }
        return normalized;
    }

    /**
     * 规范化首页样式
     *
     * @param homepageStyle 原始首页样式
     * @return 规范化首页样式
     */
    private String normalizeHomepageStyle(String homepageStyle) {
        String normalized = normalizeText(homepageStyle);
        if (!"card".equals(normalized) && !"list".equals(normalized)) {
            throw new BusinessException(AuthErrorCode.USER_SETTING_INVALID_PARAM.getCode(), AuthErrorCode.USER_SETTING_INVALID_PARAM.getMessage());
        }
        return normalized;
    }

    /**
     * 字符串转整数
     *
     * @param value 字符串值
     * @return 整数值
     */
    private Integer toInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        return userAccount;
    }

    /**
     * 获取当前登录可用用户（包含用户名修改时间字段）
     *
     * @return 用户账号
     */
    private UserAccount getCurrentActiveUserWithUsernameUpdateTime() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .select(UserAccount::getId,
                        UserAccount::getUsername,
                        UserAccount::getDeleted,
                        UserAccount::getStatus,
                        UserAccount::getUsernameUpdateTime)
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, 0)
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }
        return userAccount;
    }

    /**
     * 判断两个时间是否在同一自然月
     *
     * @param first 第一个时间
     * @param second 第二个时间
     * @return true-同月，false-不同月
     */
    private boolean isSameMonth(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return false;
        }
        return YearMonth.from(first).equals(YearMonth.from(second));
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
