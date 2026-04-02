package com.yyyouth.service.file;

import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.model.vo.file.MinioUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * MinIO 文件服务
 */
public interface MinioFileService {

    /**
     * 上传头像文件
     *
     * @param file 文件
     * @param businessName 业务名前缀
     * @return 上传结果
     */
    UserAvatarUploadVO uploadAvatar(MultipartFile file, String businessName);

    /**
     * 上传图片文件（按模块/业务/作用/月分段路径）
     *
     * @param file 图片文件
     * @param module 模块
     * @param business 业务
     * @param purpose 作用
     * @param maxFileSize 最大文件大小（字节）
     * @return 上传结果
     */
    MinioUploadVO uploadImageByMonth(
            MultipartFile file,
            String module,
            String business,
            String purpose,
            long maxFileSize
    );

    /**
     * 通过对象键删除文件
     *
     * @param objectKey 对象键
     */
    void deleteByObjectKey(String objectKey);

    /**
     * 通过文件 URL 删除文件
     *
     * @param fileUrl 文件地址
     */
    void deleteByUrl(String fileUrl);
}
