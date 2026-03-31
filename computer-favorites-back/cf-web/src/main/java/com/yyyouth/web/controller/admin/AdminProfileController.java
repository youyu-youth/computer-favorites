package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminProfileUpdateDTO;
import com.yyyouth.model.vo.admin.AdminProfileVO;
import com.yyyouth.service.admin.profile.AdminProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员资料接口控制器
 */
@Slf4j
@Api(tags = "管理员资料接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    /**
     * 查询当前登录管理员资料
     *
     * @return 管理员资料
     */
    @ApiOperation(value = "查询当前登录管理员资料")
    @GetMapping("/current")
    @SaCheckPermission(value = "admin:profile:detail", type = "admin")
    public HttpResult queryLoginAdminProfile() {
        log.info("查询管理员资料请求");
        AdminProfileVO profileVO = adminProfileService.queryLoginAdminProfile();
        log.info("查询管理员资料成功，adminId={}", profileVO.getId());
        return HttpResult.success("查询成功", profileVO);
    }

    /**
     * 更新当前登录管理员资料
     *
     * @param updateDTO 更新参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新当前登录管理员资料")
    @PutMapping({"", "/"})
    @SaCheckPermission(value = "admin:profile:edit", type = "admin")
    public HttpResult updateLoginAdminProfile(@RequestBody @Valid AdminProfileUpdateDTO updateDTO) {
        log.info("更新管理员资料请求");
        adminProfileService.updateLoginAdminProfile(updateDTO);
        log.info("更新管理员资料成功");
        return HttpResult.success("保存成功");
    }
}