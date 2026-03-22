package com.yyyouth.service.file;

import com.yyyouth.model.vo.user.UserAvatarUploadVO;
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
