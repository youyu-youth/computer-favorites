package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-06
 *
 * 用户投稿网站图标上传结果
 */
@Data
public class UserWebsiteSubmissionIconUploadVO {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 图标访问地址
     */
    private String iconUrl;
}
