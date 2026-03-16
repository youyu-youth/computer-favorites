package com.yyyouth.web.controller.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 认证接口控制器
 */
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
    @PostMapping("/login")
    public HttpResult login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        AuthLoginVO authLoginVO = authService.login(loginDTO);
        return HttpResult.success("登录成功", authLoginVO);
    }

    /**
     * 续期接口
     *
     * @return 执行结果
     */
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
    @GetMapping("/session/current")
    @SaCheckPermission("auth:session:detail")
    public HttpResult currentSession() {
        AuthSessionVO sessionVO = authService.currentSession();
        return HttpResult.success("查询成功", sessionVO);
    }
}
