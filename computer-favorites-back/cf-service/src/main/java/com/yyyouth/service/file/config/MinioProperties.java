package com.yyyouth.service.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * MinIO 配置属性
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务地址
     */
    private String endpointUrl;

    /**
     * MinIO 访问账号
     */
    private String accessKey;

    /**
     * MinIO 访问密钥
     */
    private String secreKey;

    /**
     * MinIO 桶名称
     */
    private String bucketName;
}
