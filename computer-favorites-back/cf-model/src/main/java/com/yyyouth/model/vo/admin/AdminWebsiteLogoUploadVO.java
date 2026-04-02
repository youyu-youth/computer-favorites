package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-02
 *
 * 管理端网站 Logo 上传结果
 */
@Data
public class AdminWebsiteLogoUploadVO {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * Logo 访问地址
     */
    private String logoUrl;
}
