package com.yyyouth.web.controller.auth;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthLoginEmailCodeSendDTO;
import com.yyyouth.model.dto.auth.AuthPasswordChangeDTO;
import com.yyyouth.model.dto.auth.AuthPasswordCodeSendDTO;
import com.yyyouth.model.dto.auth.AuthRegisterEmailCodeDTO;
import com.yyyouth.model.dto.auth.AuthRegisterDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.auth.AuthSessionService;
import com.yyyouth.service.auth.AuthenticationService;
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

    private final AuthenticationService authenticationService;

    private final AuthSessionService authSessionService;

    /**
     * 登录接口
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @ApiOperation(value = "登录接口")
    @PostMapping("/login")
    public HttpResult login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        AuthLoginVO authLoginVO = authenticationService.login(loginDTO);
        return HttpResult.success("登录成功", authLoginVO);
    }

    /**
     * 邮箱验证码登录接口
     *
     * @param loginEmailCodeDTO 登录参数
     * @return 登录结果
     */
    @ApiOperation(value = "邮箱验证码登录接口")
    @PostMapping("/login/code")
    public HttpResult loginByEmailCode(@RequestBody @Valid AuthLoginEmailCodeDTO loginEmailCodeDTO) {
        AuthLoginVO authLoginVO = authenticationService.loginByEmailCode(loginEmailCodeDTO);
        return HttpResult.success("登录成功", authLoginVO);
    }

    /**
     * 发送登录验证码
     *
     * @param emailCodeSendDTO 邮箱参数
     * @return 执行结果
     */
    @ApiOperation(value = "发送登录验证码")
    @PostMapping({"/login/code/send", "/login/code/send/"})
    public HttpResult sendLoginCode(@RequestBody @Valid AuthLoginEmailCodeSendDTO emailCodeSendDTO) {
        authenticationService.sendLoginEmailCode(emailCodeSendDTO.getEmail());
        return HttpResult.success("验证码发送成功");
    }

    /**
     * 发送登录验证码（兼容查询参数）
     *
     * @param email 邮箱
     * @return 执行结果
     */
    @ApiOperation(value = "发送登录验证码（查询参数）")
    @GetMapping("/login/code/send")
    public HttpResult sendLoginCodeByQuery(
            @RequestParam("email")
            @NotBlank(message = "邮箱不能为空")
            @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "邮箱格式不正确")
            @Size(max = 128, message = "邮箱长度不能超过128")
            String email) {
        authenticationService.sendLoginEmailCode(email);
        return HttpResult.success("验证码发送成功");
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
        authenticationService.register(registerDTO);
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
        authenticationService.sendRegisterEmailCode(emailCodeDTO.getEmail());
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
        boolean available = authenticationService.checkUsernameAvailable(username);
        return HttpResult.success("查询成功", available);
    }

    /**
     * 发送修改密码验证码
     *
     * @param codeSendDTO 邮箱参数
     * @return 执行结果
     */
    @ApiOperation(value = "发送修改密码验证码")
    @PostMapping({"/password/code/send", "/password/code/send/"})
    @SaCheckLogin
    public HttpResult sendPasswordCode(@RequestBody @Valid AuthPasswordCodeSendDTO codeSendDTO) {
        authenticationService.sendPasswordResetEmailCode(codeSendDTO.getEmail());
        return HttpResult.success("验证码发送成功");
    }

    /**
     * 修改密码
     *
     * @param changeDTO 修改密码参数
     * @return 执行结果
     */
    @ApiOperation(value = "修改密码")
    @PutMapping({"/password", "/password/"})
    @SaCheckLogin
    public HttpResult changePassword(@RequestBody @Valid AuthPasswordChangeDTO changeDTO) {
        authenticationService.changePassword(changeDTO);
        return HttpResult.success("密码修改成功");
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
        authSessionService.renewSession();
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
        authSessionService.logout();
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
        AuthSessionVO sessionVO = authSessionService.currentSession();
        return HttpResult.success("查询成功", sessionVO);
    }
}
