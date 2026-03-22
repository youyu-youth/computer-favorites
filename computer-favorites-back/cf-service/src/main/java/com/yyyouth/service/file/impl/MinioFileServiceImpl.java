package com.yyyouth.service.file.impl;

import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
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
        String contentType = file.getContentType();
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception ex) {
            log.error("上传头像失败，objectKey={}", objectKey, ex);
            throw new BusinessException(HttpStatus.ERROR, "头像上传失败，请稍后重试");
        }

        UserAvatarUploadVO uploadVO = new UserAvatarUploadVO();
        uploadVO.setObjectKey(objectKey);
        uploadVO.setAvatarUrl(buildPublicUrl(objectKey));
        return uploadVO;
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
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "头像文件类型不支持");
        }
        if (!"image/jpeg".equalsIgnoreCase(contentType)
                && !"image/jpg".equalsIgnoreCase(contentType)
                && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持 JPG、PNG 格式头像");
        }
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
