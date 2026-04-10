package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminUserPasswordResetDTO;
import com.yyyouth.model.dto.admin.AdminUserQueryDTO;
import com.yyyouth.model.dto.admin.AdminUserStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminUserDetailVO;
import com.yyyouth.model.vo.admin.AdminUserPageVO;
import com.yyyouth.model.vo.admin.AdminUserStatsVO;
import com.yyyouth.service.admin.user.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-09
 *
 * 管理端用户管理控制器
 */
@Slf4j
@Api(tags = "管理端用户管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "分页查询用户列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:user:list", type = "admin")
    public HttpResult list(@Valid AdminUserQueryDTO queryDTO) {
        log.info("管理端查询用户列表请求，keyword={}, status={}, pageNum={}, pageSize={}",
                queryDTO.getKeyword(), queryDTO.getStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        AdminUserPageVO result = adminUserService.queryUserPage(queryDTO);
        log.info("管理端查询用户列表成功，total={}", result.getTotal());
        return HttpResult.success("查询成功", result);
    }

    /**
     * 查询用户统计数据
     *
     * @return 统计数据
     */
    @ApiOperation(value = "查询用户统计数据")
    @GetMapping("/stats")
    @SaCheckPermission(value = "admin:user:stats", type = "admin")
    public HttpResult stats() {
        log.info("管理端查询用户统计数据请求");
        AdminUserStatsVO stats = adminUserService.queryUserStats();
        log.info("管理端查询用户统计数据成功，total={}, normal={}, disabled={}",
                stats.getTotal(), stats.getNormal(), stats.getDisabled());
        return HttpResult.success("查询成功", stats);
    }

    /**
     * 查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @ApiOperation(value = "查询用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "admin:user:detail", type = "admin")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端查询用户详情请求，userId={}", id);
        AdminUserDetailVO detail = adminUserService.queryUserDetail(id);
        log.info("管理端查询用户详情成功，userId={}", id);
        return HttpResult.success("查询成功", detail);
    }

    /**
     * 切换用户状态
     *
     * @param id        用户ID
     * @param updateDTO 状态参数
     * @return 操作结果
     */
    @ApiOperation(value = "切换用户状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = "admin:user:status", type = "admin")
    public HttpResult updateStatus(@PathVariable("id") @NotNull @Positive Long id,
                                   @RequestBody @Valid @NotNull AdminUserStatusUpdateDTO updateDTO) {
        log.info("管理端切换用户状态请求，userId={}, targetStatus={}", id, updateDTO.getStatus());
        adminUserService.updateUserStatus(id, updateDTO);
        log.info("管理端切换用户状态成功，userId={}", id);
        return HttpResult.success("状态更新成功");
    }

    /**
     * 重置用户密码
     *
     * @param id       用户ID
     * @param resetDTO 密码参数
     * @return 操作结果
     */
    @ApiOperation(value = "重置用户密码")
    @PutMapping("/{id}/password/reset")
    @SaCheckPermission(value = "admin:user:password", type = "admin")
    public HttpResult resetPassword(@PathVariable("id") @NotNull @Positive Long id,
                                    @RequestBody @Valid @NotNull AdminUserPasswordResetDTO resetDTO) {
        log.info("管理端重置用户密码请求，userId={}", id);
        adminUserService.resetUserPassword(id, resetDTO);
        log.info("管理端重置用户密码成功，userId={}", id);
        return HttpResult.success("密码重置成功");
    }

    /**
     * 踢出用户所有在线会话
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @ApiOperation(value = "踢出用户所有在线会话")
    @DeleteMapping("/{id}/sessions")
    @SaCheckPermission(value = "admin:user:kick", type = "admin")
    public HttpResult kickSessions(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端踢出用户会话请求，userId={}", id);
        adminUserService.kickUserSessions(id);
        log.info("管理端踢出用户会话成功，userId={}", id);
        return HttpResult.success("踢出成功");
    }
}
