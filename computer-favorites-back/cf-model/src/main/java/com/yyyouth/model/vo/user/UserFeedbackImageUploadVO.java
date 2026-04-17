package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈图片上传结果
 */
@Data
public class UserFeedbackImageUploadVO {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 图片访问地址
     */
    private String imageUrl;
}
