package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminAnnouncementCreateDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementEditDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementQueryDTO;
import com.yyyouth.model.dto.admin.AdminAnnouncementStatusDTO;
import com.yyyouth.model.vo.admin.AdminAnnouncementPageVO;
import com.yyyouth.service.admin.announcement.AdminAnnouncementService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-22
 *
 * 管理端公告管理控制器
 */
@Slf4j
@Api(tags = "管理端公告管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/announcement")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    /**
     * 查询公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据（含统计信息）
     */
    @ApiOperation(value = "查询公告分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:announcement:list", type = "admin")
    public HttpResult list(@Valid AdminAnnouncementQueryDTO queryDTO) {
        log.info("查询管理端公告列表请求，pageNum={}, pageSize={}, keyword={}, status={}, type={}, isTop={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword(),
                queryDTO.getStatus(), queryDTO.getType(), queryDTO.getIsTop());
        AdminAnnouncementPageVO pageVO = adminAnnouncementService.queryAnnouncementPage(queryDTO);
        log.info("查询管理端公告列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 新增公告
     *
     * @param createDTO 创建参数
     * @return 公告ID
     */
    @ApiOperation(value = "新增公告")
    @PostMapping({"", "/"})
    @SaCheckPermission(value = "admin:announcement:add", type = "admin")
    public HttpResult createAnnouncement(@RequestBody @Valid @NotNull AdminAnnouncementCreateDTO createDTO) {
        log.info("管理端新增公告请求，title={}, type={}", createDTO.getTitle(), createDTO.getType());
        Long announcementId = adminAnnouncementService.createAnnouncement(createDTO);
        log.info("管理端新增公告成功，announcementId={}", announcementId);
        return HttpResult.success("新增公告成功", announcementId);
    }

    /**
     * 编辑公告
     *
     * @param id 公告ID
     * @param editDTO 编辑参数
     * @return 执行结果
     */
    @ApiOperation(value = "编辑公告")
    @PutMapping("/{id}")
    @SaCheckPermission(value = "admin:announcement:edit", type = "admin")
    public HttpResult editAnnouncement(@PathVariable("id") @NotNull @Positive Long id,
                                       @RequestBody @Valid @NotNull AdminAnnouncementEditDTO editDTO) {
        log.info("管理端编辑公告请求，id={}, title={}, type={}", id, editDTO.getTitle(), editDTO.getType());
        adminAnnouncementService.editAnnouncement(id, editDTO);
        log.info("管理端编辑公告成功，id={}", id);
        return HttpResult.success("编辑公告成功");
    }

    /**
     * 更新公告状态
     *
     * @param id 公告ID
     * @param statusDTO 状态参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新公告状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = "admin:announcement:edit", type = "admin")
    public HttpResult updateAnnouncementStatus(@PathVariable("id") @NotNull @Positive Long id,
                                               @RequestBody @Valid @NotNull AdminAnnouncementStatusDTO statusDTO) {
        log.info("管理端更新公告状态请求，id={}, status={}", id, statusDTO.getStatus());
        adminAnnouncementService.updateAnnouncementStatus(id, statusDTO);
        log.info("管理端更新公告状态成功，id={}", id);
        return HttpResult.success("状态更新成功");
    }

    /**
     * 删除公告（逻辑删除）
     *
     * @param id 公告ID
     * @return 执行结果
     */
    @ApiOperation(value = "删除公告")
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "admin:announcement:delete", type = "admin")
    public HttpResult deleteAnnouncement(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端删除公告请求，id={}", id);
        adminAnnouncementService.deleteAnnouncement(id);
        log.info("管理端删除公告成功，id={}", id);
        return HttpResult.success("公告删除成功");
    }
}
