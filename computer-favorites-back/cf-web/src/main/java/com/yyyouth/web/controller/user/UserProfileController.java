package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserProfileUpdateDTO;
import com.yyyouth.model.dto.user.UserUsernameUpdateDTO;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserAvatarUploadVO;
import com.yyyouth.service.user.UserProfileService;
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
public class UserProfileController {

    private final UserProfileService userProfileService;

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
