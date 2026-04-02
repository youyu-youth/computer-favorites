package com.yyyouth.service.file.impl;

import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.file.config.MinioProperties;
import com.yyyouth.service.file.util.MinioObjectKeyUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * MinIO 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileServiceImpl implements MinioFileService {

    private static final long MAX_AVATAR_FILE_SIZE = 2 * 1024 * 1024;

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Set<String> AVATAR_ALLOWED_IMAGE_TYPES = Set.of(
        "jpeg",
        "png"
    );

    private static final Set<String> IMAGE_ALLOWED_IMAGE_TYPES = Set.of(
        "jpeg",
        "png",
        "webp"
    );

    private static final byte[] JPEG_PREFIX = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private static final byte[] PNG_PREFIX = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private static final byte[] RIFF_PREFIX = new byte[] {0x52, 0x49, 0x46, 0x46};

    private static final byte[] WEBP_PREFIX = new byte[] {0x57, 0x45, 0x42, 0x50};

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    /**
     * 上传头像文件
     *
     * @param file 文件
     * @param businessName 业务名前缀
     * @return 上传结果
     */
    @Override
    public UserAvatarUploadVO uploadAvatar(MultipartFile file, String businessName) {
        validateAvatarFile(file);
        ensureBucketExists();

        String objectKey = MinioObjectKeyUtils.buildObjectKey(businessName, file.getOriginalFilename());
        MinioUploadVO minioUploadVO = uploadToMinio(file, objectKey, "头像上传失败，请稍后重试");

        UserAvatarUploadVO uploadVO = new UserAvatarUploadVO();
        uploadVO.setObjectKey(minioUploadVO.getObjectKey());
        uploadVO.setAvatarUrl(minioUploadVO.getFileUrl());
        return uploadVO;
    }

    /**
     * 上传图片文件（按模块/业务/作用/月分段路径）
     *
     * @param file 图片文件
     * @param module 模块
     * @param business 业务
     * @param purpose 作用
     * @param maxFileSize 最大文件大小
     * @return 上传结果
     */
    @Override
    public MinioUploadVO uploadImageByMonth(
            MultipartFile file,
            String module,
            String business,
            String purpose,
            long maxFileSize
    ) {
        validateImageFile(file, maxFileSize);
        ensureBucketExists();
        String objectKey = MinioObjectKeyUtils.buildObjectKeyByMonth(module, business, purpose, file.getOriginalFilename());
        return uploadToMinio(file, objectKey, "图片上传失败，请稍后重试");
    }

    /**
     * 通过对象键删除文件
     *
     * @param objectKey 对象键
     */
    @Override
    public void deleteByObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.warn("删除 MinIO 文件失败，objectKey={}", objectKey, ex);
        }
    }

    /**
     * 通过文件 URL 删除文件
     *
     * @param fileUrl 文件地址
     */
    @Override
    public void deleteByUrl(String fileUrl) {
        String objectKey = MinioObjectKeyUtils.extractObjectKeyFromUrl(fileUrl, minioProperties.getBucketName());
        deleteByObjectKey(objectKey);
    }

    /**
     * 校验头像文件
     *
     * @param file 文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择要上传的头像文件");
        }
        if (file.getSize() > MAX_AVATAR_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "头像大小不能超过2MB");
        }
        String imageType = detectImageType(file);
        if (!AVATAR_ALLOWED_IMAGE_TYPES.contains(imageType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持 JPG、PNG 格式头像");
        }
    }

    /**
     * 校验图片文件
     *
     * @param file 图片文件
     * @param maxFileSize 最大文件大小（字节）
     */
    private void validateImageFile(MultipartFile file, long maxFileSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择要上传的图片文件");
        }
        if (file.getSize() > maxFileSize) {
            long maxSizeMb = Math.max(1L, maxFileSize / (1024 * 1024));
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图片大小不能超过" + maxSizeMb + "MB");
        }

        String imageType = detectImageType(file);
        if (!IMAGE_ALLOWED_IMAGE_TYPES.contains(imageType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持 JPG、PNG、WebP 格式图片");
        }
    }

    /**
     * 执行 MinIO 上传
     *
     * @param file 文件
     * @param objectKey 对象键
     * @param failureMessage 失败提示
     * @return 上传结果
     */
    private MinioUploadVO uploadToMinio(MultipartFile file, String objectKey, String failureMessage) {
        String contentType = normalizeContentType(file.getContentType());
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(StringUtils.hasText(contentType) ? contentType : DEFAULT_CONTENT_TYPE)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception ex) {
            log.error("上传文件失败，objectKey={}", objectKey, ex);
            throw new BusinessException(HttpStatus.ERROR, failureMessage);
        }

        MinioUploadVO uploadVO = new MinioUploadVO();
        uploadVO.setObjectKey(objectKey);
        uploadVO.setFileUrl(buildPublicUrl(objectKey));
        return uploadVO;
    }

    /**
     * 标准化 content-type
     *
     * @param contentType 原始类型
     * @return 标准化结果
     */
    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 检测图片类型
     *
     * @param file 文件
     * @return 图片类型：jpeg/png/webp，无法识别返回空字符串
     */
    private String detectImageType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            if (matchesPrefix(header, JPEG_PREFIX)) {
                return "jpeg";
            }
            if (matchesPrefix(header, PNG_PREFIX)) {
                return "png";
            }
            if (isWebp(header)) {
                return "webp";
            }
            return "";
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图片文件读取失败");
        }
    }

    /**
     * 校验字节前缀
     *
     * @param source 源字节
     * @param prefix 前缀字节
     * @return 是否匹配
     */
    private boolean matchesPrefix(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为 webp
     *
     * @param header 头字节
     * @return 是否为webp
     */
    private boolean isWebp(byte[] header) {
        if (header.length < 12) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (header[i] != RIFF_PREFIX[i]) {
                return false;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (header[i + 8] != WEBP_PREFIX[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 确保桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            }
        } catch (Exception ex) {
            log.error("检查或创建 MinIO 桶失败，bucket={}", minioProperties.getBucketName(), ex);
            throw new BusinessException(HttpStatus.ERROR, "文件存储服务不可用，请稍后重试");
        }
    }

    /**
     * 构建访问地址
     *
     * @param objectKey 对象键
     * @return 访问地址
     */
    private String buildPublicUrl(String objectKey) {
        String endpoint = minioProperties.getEndpointUrl();
        if (!StringUtils.hasText(endpoint)) {
            return objectKey;
        }
        String normalizedEndpoint = endpoint.endsWith("/")
                ? endpoint.substring(0, endpoint.length() - 1)
                : endpoint;
        return normalizedEndpoint + "/" + minioProperties.getBucketName() + "/" + objectKey;
    }
}
