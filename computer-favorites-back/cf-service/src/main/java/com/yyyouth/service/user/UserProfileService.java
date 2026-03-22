package com.yyyouth.service.user;

import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料服务接口
 */
public interface UserProfileService {

    /**
     * 查询登录用户资料
     *
     * @return 登录用户资料
     */
    LoginUserProfileVO queryLoginUserProfile();

    /**
     * 更新登录用户资料
     *
     * @param updateDTO 更新参数
     */
    void updateLoginUserProfile(UserProfileUpdateDTO updateDTO);

    /**
     * 上传登录用户头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    UserAvatarUploadVO uploadLoginUserAvatar(MultipartFile file);

    /**
     * 删除登录用户头像
     */
    void deleteLoginUserAvatar();
}
