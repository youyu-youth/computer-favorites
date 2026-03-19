package com.yyyouth.web.controller.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthRegisterEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.auth.impl.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证接口控制器
 */
@Api(tags = "认证接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @ApiOperation(value = "登录接口")
    @PostMapping("/login")
    public HttpResult login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        AuthLoginVO authLoginVO = authService.login(loginDTO);
        return HttpResult.success("登录成功", authLoginVO);
    }

    /**
     * 注册接口
     *
     * @param registerDTO 注册参数
     * @return 执行结果
     */
    @ApiOperation(value = "注册接口")
    @PostMapping("/register")
    public HttpResult register(@RequestBody @Valid AuthRegisterDTO registerDTO) {
        authService.register(registerDTO);
        return HttpResult.success("注册成功");
    }

    /**
     * 发送注册验证码
     *
     * @param emailCodeDTO 邮箱参数
     * @return 执行结果
     */
    @ApiOperation(value = "发送注册验证码")
    @PostMapping("/register/code")
    public HttpResult sendRegisterCode(@RequestBody @Valid AuthRegisterEmailCodeDTO emailCodeDTO) {
        authService.sendRegisterEmailCode(emailCodeDTO.getEmail());
        return HttpResult.success("验证码发送成功");
    }

    /**
     * 校验用户名是否可用
     *
     * @param username 用户名
     * @return 可用状态
     */
    @ApiOperation(value = "校验用户名是否可用")
    @GetMapping("/register/username/check")
    public HttpResult checkRegisterUsername(
            @RequestParam("username")
            @NotBlank(message = "用户名不能为空")
            @Size(min = 4, max = 24, message = "用户名长度需在4-24之间")
            @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅支持字母、数字和下划线")
            String username) {
        boolean available = authService.checkUsernameAvailable(username);
        return HttpResult.success("查询成功", available);
    }

    /**
     * 续期接口
     *
     * @return 执行结果
     */
    @ApiOperation(value = "续期接口")
    @PutMapping("/session/renew")
    @SaCheckPermission("auth:session:renew")
    public HttpResult renewSession() {
        authService.renewSession();
        return HttpResult.success("续期成功");
    }

    /**
     * 退出接口
     *
     * @return 执行结果
     */
    @ApiOperation(value = "退出接口")
    @DeleteMapping("/session")
    @SaCheckPermission("auth:session:delete")
    public HttpResult logout() {
        authService.logout();
        return HttpResult.success("退出成功");
    }

    /**
     * 查询当前会话
     *
     * @return 当前会话
     */
    @ApiOperation(value = "查询当前会话")
    @GetMapping("/session/current")
    @SaCheckPermission("auth:session:detail")
    public HttpResult currentSession() {
        AuthSessionVO sessionVO = authService.currentSession();
        return HttpResult.success("查询成功", sessionVO);
    }
}
