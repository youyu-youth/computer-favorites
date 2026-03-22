package com.yyyouth.service.file.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * MinIO 对象键工具类
 */
public final class MinioObjectKeyUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private MinioObjectKeyUtils() {
    }

    /**
     * 生成对象键
     *
     * @param businessName 业务名称
     * @param originalFilename 原始文件名
     * @return 对象键
     */
    public static String buildObjectKey(String businessName, String originalFilename) {
        String normalizedBusinessName = sanitizePathPart(businessName);
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String extension = resolveExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(extension)) {
            return normalizedBusinessName + "/" + datePart + "/" + uuid + "." + extension;
        }
        return normalizedBusinessName + "/" + datePart + "/" + uuid;
    }

    /**
     * 从 URL 中提取对象键
     *
     * @param fileUrl 文件地址
     * @param bucketName 桶名
     * @return 对象键
     */
    public static String extractObjectKeyFromUrl(String fileUrl, String bucketName) {
        if (!StringUtils.hasText(fileUrl)) {
            return "";
        }

        String trimmed = fileUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return trimmed;
        }

        try {
            URI uri = URI.create(trimmed);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return "";
            }
            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            String bucketPrefix = bucketName + "/";
            if (normalizedPath.startsWith(bucketPrefix)) {
                String objectKey = normalizedPath.substring(bucketPrefix.length());
                return URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 规范化路径片段
     *
     * @param value 原始值
     * @return 规范化结果
     */
    private static String sanitizePathPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "default";
        }
        return value.trim().replaceAll("[^a-zA-Z0-9-_]", "-").toLowerCase(Locale.ROOT);
    }

    /**
     * 解析文件后缀
     *
     * @param originalFilename 原始文件名
     * @return 后缀
     */
    private static String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).trim();
        return extension.toLowerCase(Locale.ROOT);
    }
}
