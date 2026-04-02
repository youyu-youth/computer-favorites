package com.yyyouth.model.vo.file;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * MinIO 上传结果
 */
@Data
public class MinioUploadVO {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 文件访问地址
     */
    private String fileUrl;
}
