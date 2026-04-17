package com.yyyouth.service.user.notification.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.utils.SensitiveWordUtils;
import com.yyyouth.model.dto.user.UserFeedbackCreateDTO;
import com.yyyouth.model.enums.FeedbackStatus;
import com.yyyouth.model.enums.FeedbackType;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.system.Feedback;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.model.vo.user.UserFeedbackImageUploadVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.system.FeedbackMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.user.notification.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private static final int ENABLED_STATUS = 1;

    private static final int NOT_DELETED = 0;

    private static final String IMAGE_PATH_MODULE = "user";

    private static final String IMAGE_PATH_BUSINESS = "feedback";

    private static final String IMAGE_PATH_PURPOSE = "image";

    private static final String IMAGE_OBJECT_KEY_PREFIX =
            IMAGE_PATH_MODULE + "/" + IMAGE_PATH_BUSINESS + "/" + IMAGE_PATH_PURPOSE + "/";

    private static final long MAX_IMAGE_FILE_SIZE = 2 * 1024 * 1024L;

    private final FeedbackMapper feedbackMapper;

    private final UserAccountMapper userAccountMapper;

    private final MinioFileService minioFileService;

    /**
     * 上传反馈图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @Override
    public UserFeedbackImageUploadVO uploadFeedbackImage(MultipartFile file) {
        getCurrentActiveUser();
        MinioUploadVO uploadVO = minioFileService.uploadImageByMonth(
                file,
                IMAGE_PATH_MODULE,
                IMAGE_PATH_BUSINESS,
                IMAGE_PATH_PURPOSE,
                MAX_IMAGE_FILE_SIZE
        );
        UserFeedbackImageUploadVO resultVO = new UserFeedbackImageUploadVO();
        resultVO.setObjectKey(uploadVO.getObjectKey());
        resultVO.setImageUrl(uploadVO.getFileUrl());
        return resultVO;
    }

    /**
     * 删除反馈图片
     *
     * @param objectKey 对象键
     */
    @Override
    public void deleteFeedbackImage(String objectKey) {
        getCurrentActiveUser();
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        minioFileService.deleteByObjectKey(normalizedObjectKey);
    }

    /**
     * 提交反馈
     *
     * @param createDTO 反馈参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(UserFeedbackCreateDTO createDTO) {
        UserAccount currentUser = getCurrentActiveUser();
        validateFeedbackType(createDTO.getType());

        String normalizedContent = normalizeRequiredText(createDTO.getContent(), "反馈内容不能为空");
        validateSensitiveWord(normalizedContent, "反馈内容包含敏感词");

        String normalizedContact = normalizeOptionalText(createDTO.getContact());
        if (StringUtils.hasText(normalizedContact)) {
            validateSensitiveWord(normalizedContact, "联系方式包含敏感词");
        }

        List<String> normalizedImages = normalizeImageUrls(createDTO.getImages());
        Feedback feedback = Feedback.builder()
                .userId(currentUser.getId())
                .avatar(resolveFeedbackAvatar(currentUser.getAvatar()))
                .contact(normalizedContact)
                .type(createDTO.getType())
                .content(normalizedContent)
                .images(CollectionUtils.isEmpty(normalizedImages) ? null : JSONUtil.toJsonStr(normalizedImages))
                .status(FeedbackStatus.PENDING.getCode())
                .build();

        int insertedRows = feedbackMapper.insert(feedback);
        if (insertedRows != 1 || feedback.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "反馈提交失败，请稍后重试");
        }
        log.info("用户反馈提交成功，userId={}, feedbackId={}, type={}",
                currentUser.getId(), feedback.getId(), feedback.getType());
    }

    /**
     * 获取当前启用中的登录用户
     *
     * @return 用户信息
     */
    private UserAccount getCurrentActiveUser() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, NOT_DELETED)
                .eq(UserAccount::getStatus, ENABLED_STATUS)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户未登录或账号不可用");
        }
        return userAccount;
    }

    /**
     * 校验反馈类型
     *
     * @param type 类型编码
     */
    private void validateFeedbackType(Integer type) {
        if (!FeedbackType.isValid(type)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "反馈类型不合法");
        }
    }

    /**
     * 归一化必填文本
     *
     * @param value 原始文本
     * @param emptyMessage 为空提示
     * @return 归一化结果
     */
    private String normalizeRequiredText(String value, String emptyMessage) {
        String normalizedValue = normalizeOptionalText(value);
        if (!StringUtils.hasText(normalizedValue)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, emptyMessage);
        }
        return normalizedValue;
    }

    /**
     * 归一化可选文本
     *
     * @param value 原始文本
     * @return 归一化结果
     */
    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 归一化图片地址列表
     *
     * @param imageUrls 原始图片地址
     * @return 归一化后的图片地址列表
     */
    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return List.of();
        }
        return imageUrls.stream()
                .map(this::normalizeRequiredImageUrl)
                .distinct()
                .toList();
    }

    /**
     * 归一化单个图片地址
     *
     * @param imageUrl 原始图片地址
     * @return 归一化图片地址
     */
    private String normalizeRequiredImageUrl(String imageUrl) {
        String normalizedUrl = normalizeRequiredText(imageUrl, "图片地址不能为空");
        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图片地址不合法，请重新上传");
        }
        return normalizedUrl;
    }

    /**
     * 归一化反馈图片对象键
     *
     * @param objectKey 原始对象键
     * @return 归一化后的对象键
     */
    private String normalizeObjectKey(String objectKey) {
        String normalizedObjectKey = normalizeRequiredText(objectKey, "对象键不能为空");
        if (!normalizedObjectKey.startsWith(IMAGE_OBJECT_KEY_PREFIX)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "反馈图片对象键不合法");
        }
        return normalizedObjectKey;
    }

    /**
     * 解析反馈头像地址
     *
     * @param avatar 原始头像地址
     * @return 可落库头像地址
     */
    private String resolveFeedbackAvatar(String avatar) {
        String normalizedAvatar = normalizeOptionalText(avatar);
        return normalizedAvatar == null ? "" : normalizedAvatar;
    }

    /**
     * 校验文本敏感词
     *
     * @param text 待校验文本
     * @param errorPrefix 错误提示前缀
     */
    private void validateSensitiveWord(String text, String errorPrefix) {
        if (!SensitiveWordUtils.containsForUserContent(text)) {
            return;
        }
        String sensitiveWord = SensitiveWordUtils.findFirst(text);
        String message = StringUtils.hasText(sensitiveWord)
                ? errorPrefix + "，命中词：" + sensitiveWord
                : errorPrefix;
        throw new BusinessException(HttpStatus.BAD_REQUEST, message);
    }
}
