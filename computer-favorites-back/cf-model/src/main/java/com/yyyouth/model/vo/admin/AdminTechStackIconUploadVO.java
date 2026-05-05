package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈图标上传结果
 */
@Data
public class AdminTechStackIconUploadVO {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 图标访问地址
     */
    private String iconUrl;
}
