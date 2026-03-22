package com.yyyouth.model.vo.user;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-22
 *
 * 用户头像上传结果视图
 */
@Data
public class UserAvatarUploadVO {

    /**
     * 头像访问地址
     */
    private String avatarUrl;

    /**
     * 对象键
     */
    private String objectKey;
}
