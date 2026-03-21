package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.service.user.UserProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户资料接口控制器
 */
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
}
