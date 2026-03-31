package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.auth.AuthLoginDTO;
import com.yyyouth.model.vo.auth.AuthLoginVO;
import com.yyyouth.model.vo.auth.AuthSessionVO;
import com.yyyouth.service.admin.AdminAuthenticationService;
import com.yyyouth.service.admin.AdminSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * @date 2026-03-31
 *
 * 管理员认证接口控制器
 */
@Slf4j
@Api(tags = "管理员认证接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthenticationService adminAuthenticationService;

    private final AdminSessionService adminSessionService;

    /**
     * 管理员登录接口
     *
     * @param loginDTO 登录参数
     * @param request 请求对象
     * @return 登录结果
     */
    @ApiOperation(value = "管理员登录接口")
    @PostMapping("/login")
    public HttpResult login(@RequestBody @Valid AuthLoginDTO loginDTO, HttpServletRequest request) {
        log.info("管理员登录请求，username={}", loginDTO.getUsername());
        AuthLoginVO authLoginVO = adminAuthenticationService.login(loginDTO, request.getRemoteAddr());
        Long adminId = authLoginVO.getUserInfo() == null ? null : authLoginVO.getUserInfo().getUserId();
        log.info("管理员登录成功，adminId={}", adminId);
        return HttpResult.success("登录成功", authLoginVO);
    }

    /**
     * 管理员会话续期
     *
     * @return 执行结果
     */
    @ApiOperation(value = "管理员会话续期")
    @PutMapping("/session/renew")
    @SaCheckPermission(value = "admin:auth:renew", type = "admin")
    public HttpResult renewSession() {
        log.info("管理员会话续期请求");
        adminSessionService.renewSession();
        log.info("管理员会话续期成功");
        return HttpResult.success("续期成功");
    }

    /**
     * 管理员退出登录
     *
     * @return 执行结果
     */
    @ApiOperation(value = "管理员退出接口")
    @DeleteMapping("/session")
    @SaCheckPermission(value = "admin:auth:delete", type = "admin")
    public HttpResult logout() {
        log.info("管理员退出请求");
        adminSessionService.logout();
        log.info("管理员退出成功");
        return HttpResult.success("退出成功");
    }

    /**
     * 查询管理员当前会话
     *
     * @return 当前会话
     */
    @ApiOperation(value = "查询管理员当前会话")
    @GetMapping("/session/current")
    @SaCheckPermission(value = "admin:auth:detail", type = "admin")
    public HttpResult currentSession() {
        log.info("查询管理员当前会话请求");
        AuthSessionVO sessionVO = adminSessionService.currentSession();
        log.info("查询管理员当前会话成功，adminId={}", sessionVO.getUserId());
        return HttpResult.success("查询成功", sessionVO);
    }
}
