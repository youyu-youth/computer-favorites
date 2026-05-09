package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.ProfilePrivacyUpdateDTO;
import com.yyyouth.model.dto.user.UserEmailCodeSendDTO;
import com.yyyouth.model.dto.user.UserEmailUpdateDTO;
import com.yyyouth.model.dto.user.UserPreferenceSettingUpdateDTO;
import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.dto.user.UserUsernameUpdateDTO;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.service.audit.annotation.AuditLog;
import com.yyyouth.service.user.profile.UserProfileService;
import com.yyyouth.service.user.userstats.ProfilePrivacyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户资料接口控制器
 */
@Slf4j
@Api(tags = "用户资料接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/profile")
@AuditLog(module = "user-profile-dashboard")
public class UserProfileController {

    private final UserProfileService userProfileService;

    private final ProfilePrivacyService profilePrivacyService;

    /**
     * 查询登录用户资料
     *
     * @return 登录用户资料
     */
    @ApiOperation(value = "查询登录用户资料")
    @GetMapping("/current")
    @SaCheckLogin
    public HttpResult queryLoginUserProfile() {
        LoginUserProfileVO resultVO = userProfileService.queryLoginUserProfile();
        return HttpResult.success("查询成功", resultVO);
    }

    /**
     * 编辑登录用户资料
     *
     * @param updateDTO 更新参数
     * @return 执行结果
     */
    @ApiOperation(value = "编辑登录用户资料")
    @PutMapping({"", "/", "/update"})
    @SaCheckLogin
    public HttpResult updateLoginUserProfile(@RequestBody @Valid UserProfileUpdateDTO updateDTO) {
        userProfileService.updateLoginUserProfile(updateDTO);
        return HttpResult.success("保存成功");
    }

    /**
     * 修改登录用户用户名
     *
     * @param updateDTO 用户名修改参数
     * @return 执行结果
     */
    @ApiOperation(value = "修改登录用户用户名")
    @PutMapping("/username")
    @SaCheckLogin
    public HttpResult updateLoginUsername(@RequestBody @Valid UserUsernameUpdateDTO updateDTO) {
        log.info("收到用户名修改请求");
        userProfileService.updateLoginUsername(updateDTO);
        return HttpResult.success("用户名修改成功");
    }

    /**
     * 更新登录用户偏好设置
     *
     * @param updateDTO 偏好设置参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新登录用户偏好设置")
    @PutMapping("/setting")
    @SaCheckLogin
    public HttpResult updateLoginUserSetting(@RequestBody @Valid UserPreferenceSettingUpdateDTO updateDTO) {
        log.info("收到偏好设置更新请求");
        userProfileService.updateLoginUserSetting(updateDTO);
        return HttpResult.success("偏好设置保存成功");
    }

    /**
     * 更新登录用户主页隐私设置（user-15 M5）。
     *
     * 仅维护 t_user_setting 中 3 个隐私字段，与偏好设置接口解耦。
     *
     * @param updateDTO 隐私设置参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新登录用户主页隐私设置")
    @PutMapping("/privacy")
    @SaCheckLogin
    @AuditLog(action = "privacy", description = "更新用户主页隐私设置")
    public HttpResult updateProfilePrivacy(@RequestBody @Valid ProfilePrivacyUpdateDTO updateDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("收到主页隐私设置更新请求 userId={}", userId);
        profilePrivacyService.updatePrivacy(userId, updateDTO);
        return HttpResult.success("隐私设置保存成功");
    }

    /**
     * 发送邮箱修改验证码
     *
     * @param sendDTO 发送参数
     * @return 执行结果
     */
    @ApiOperation(value = "发送邮箱修改验证码")
    @PostMapping("/email/code/send")
    @SaCheckLogin
    public HttpResult sendEmailUpdateCode(@RequestBody @Valid UserEmailCodeSendDTO sendDTO) {
        log.info("收到邮箱修改验证码发送请求");
        userProfileService.sendEmailUpdateCode(sendDTO);
        return HttpResult.success("验证码发送成功");
    }

    /**
     * 校验邮箱修改验证码
     *
     * @param updateDTO 校验参数
     * @return 执行结果
     */
    @ApiOperation(value = "校验邮箱修改验证码")
    @PostMapping("/email/code/verify")
    @SaCheckLogin
    public HttpResult verifyEmailUpdateCode(@RequestBody @Valid UserEmailUpdateDTO updateDTO) {
        log.info("收到邮箱修改验证码校验请求");
        userProfileService.verifyEmailUpdateCode(updateDTO);
        return HttpResult.success("验证码校验通过");
    }

    /**
     * 修改登录用户邮箱
     *
     * @param updateDTO 邮箱修改参数
     * @return 执行结果
     */
    @ApiOperation(value = "修改登录用户邮箱")
    @PutMapping("/email")
    @SaCheckLogin
    public HttpResult updateLoginEmail(@RequestBody @Valid UserEmailUpdateDTO updateDTO) {
        log.info("收到邮箱修改请求");
        userProfileService.updateLoginEmail(updateDTO);
        return HttpResult.success("邮箱修改成功");
    }

    /**
     * 上传登录用户头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传登录用户头像")
    @PostMapping("/avatar")
    @SaCheckLogin
    public HttpResult uploadLoginUserAvatar(@RequestParam("file") MultipartFile file) {
        UserAvatarUploadVO uploadVO = userProfileService.uploadLoginUserAvatar(file);
        return HttpResult.success("上传成功", uploadVO);
    }

    /**
     * 删除登录用户头像
     *
     * @return 删除结果
     */
    @ApiOperation(value = "删除登录用户头像")
    @DeleteMapping("/avatar")
    @SaCheckLogin
    public HttpResult deleteLoginUserAvatar() {
        userProfileService.deleteLoginUserAvatar();
        return HttpResult.success("删除成功");
    }
}
