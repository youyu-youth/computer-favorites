package com.yyyouth.service.user;

import com.yyyouth.model.dto.user.UserEmailCodeSendDTO;
import com.yyyouth.model.dto.user.UserEmailUpdateDTO;
import com.yyyouth.model.dto.user.UserPreferenceSettingUpdateDTO;
import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.dto.user.UserUsernameUpdateDTO;
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
     * 修改登录用户用户名
     *
     * @param updateDTO 用户名修改参数
     */
    void updateLoginUsername(UserUsernameUpdateDTO updateDTO);

    /**
     * 更新登录用户偏好设置
     *
     * @param updateDTO 偏好设置参数
     */
    void updateLoginUserSetting(UserPreferenceSettingUpdateDTO updateDTO);

    /**
     * 发送邮箱修改验证码
     *
     * @param sendDTO 发送参数
     */
    void sendEmailUpdateCode(UserEmailCodeSendDTO sendDTO);

    /**
     * 校验邮箱修改验证码
     *
     * @param updateDTO 校验参数
     */
    void verifyEmailUpdateCode(UserEmailUpdateDTO updateDTO);

    /**
     * 修改登录用户邮箱
     *
     * @param updateDTO 邮箱修改参数
     */
    void updateLoginEmail(UserEmailUpdateDTO updateDTO);

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
