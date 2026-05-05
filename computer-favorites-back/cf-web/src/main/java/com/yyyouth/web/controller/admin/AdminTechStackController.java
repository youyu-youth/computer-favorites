package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminTechStackBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTechStackCreateDTO;
import com.yyyouth.model.dto.admin.AdminTechStackEditDTO;
import com.yyyouth.model.dto.admin.AdminTechStackQueryDTO;
import com.yyyouth.model.dto.admin.AdminTechStackStatusDTO;
import com.yyyouth.model.vo.admin.AdminTechStackPageVO;
import com.yyyouth.model.vo.admin.AdminTechStackStatsVO;
import com.yyyouth.model.vo.admin.AdminTechStackIconUploadVO;
import com.yyyouth.service.admin.techstack.AdminTechStackService;
import com.yyyouth.service.audit.annotation.AuditLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈管理控制器
 */
@Slf4j
@Api(tags = "管理端技术栈管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tech-stack")
@AuditLog(module = "admin-tech-stack")
public class AdminTechStackController {

    private final AdminTechStackService adminTechStackService;

    /**
     * 查询技术栈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询技术栈分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:techstack:list", type = "admin")
    public HttpResult list(@Valid AdminTechStackQueryDTO queryDTO) {
        log.info("查询管理端技术栈列表请求，pageNum={}, pageSize={}, keyword={}, status={}, sortField={}, sortOrder={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword(),
                queryDTO.getStatus(), queryDTO.getSortField(), queryDTO.getSortOrder());
        AdminTechStackPageVO pageVO = adminTechStackService.queryTechStackPage(queryDTO);
        log.info("查询管理端技术栈列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询技术栈统计
     *
     * @return 技术栈统计
     */
    @ApiOperation(value = "查询技术栈统计")
    @GetMapping("/stats")
    @SaCheckPermission(value = "admin:techstack:stats", type = "admin")
    public HttpResult stats() {
        log.info("查询管理端技术栈统计请求");
        AdminTechStackStatsVO statsVO = adminTechStackService.queryTechStackStats();
        log.info("查询管理端技术栈统计成功，total={}, enabled={}, disabled={}",
                statsVO.getTotal(), statsVO.getEnabled(), statsVO.getDisabled());
        return HttpResult.success("查询成功", statsVO);
    }

    /**
     * 新增技术栈
     *
     * @param createDTO 创建参数
     * @return 技术栈ID
     */
    @ApiOperation(value = "新增技术栈")
    @PostMapping({"", "/"})
    @SaCheckPermission(value = "admin:techstack:add", type = "admin")
    @AuditLog(action = "create", description = "新增技术栈，name=#{#createDTO.name}")
    public HttpResult createTechStack(@RequestBody @Valid @NotNull AdminTechStackCreateDTO createDTO) {
        log.info("管理端新增技术栈请求，name={}, sort={}", createDTO.getName(), createDTO.getSort());
        Long techStackId = adminTechStackService.createTechStack(createDTO);
        log.info("管理端新增技术栈成功，techStackId={}", techStackId);
        return HttpResult.success("新增技术栈成功", techStackId);
    }

    /**
     * 编辑技术栈
     *
     * @param id 技术栈ID
     * @param editDTO 编辑参数
     * @return 执行结果
     */
    @ApiOperation(value = "编辑技术栈")
    @PutMapping("/{id}")
    @SaCheckPermission(value = "admin:techstack:edit", type = "admin")
    @AuditLog(action = "edit", description = "编辑技术栈，id=#{#id}")
    public HttpResult editTechStack(@PathVariable("id") @NotNull @Positive Long id,
                                    @RequestBody @Valid @NotNull AdminTechStackEditDTO editDTO) {
        log.info("管理端编辑技术栈请求，id={}, name={}, sort={}", id, editDTO.getName(), editDTO.getSort());
        adminTechStackService.editTechStack(id, editDTO);
        log.info("管理端编辑技术栈成功，id={}", id);
        return HttpResult.success("编辑技术栈成功");
    }

    /**
     * 更新技术栈状态（启用/禁用）
     *
     * @param id 技术栈ID
     * @param statusDTO 状态变更参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新技术栈状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission(value = "admin:techstack:status", type = "admin")
    @AuditLog(action = "update-status", description = "切换技术栈状态，id=#{#id}，status=#{#statusDTO.status}")
    public HttpResult updateTechStackStatus(@PathVariable("id") @NotNull @Positive Long id,
                                            @RequestBody @Valid @NotNull AdminTechStackStatusDTO statusDTO) {
        log.info("管理端切换技术栈状态请求，id={}, status={}", id, statusDTO.getStatus());
        adminTechStackService.updateTechStackStatus(id, statusDTO.getStatus());
        log.info("管理端切换技术栈状态成功，id={}, status={}", id, statusDTO.getStatus());
        return HttpResult.success("操作成功");
    }

    /**
     * 删除技术栈（逻辑删除）
     *
     * @param id 技术栈ID
     * @return 执行结果
     */
    @ApiOperation(value = "删除技术栈")
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "admin:techstack:delete", type = "admin")
    @AuditLog(action = "delete", description = "删除技术栈，id=#{#id}")
    public HttpResult deleteTechStack(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端删除技术栈请求，id={}", id);
        adminTechStackService.deleteTechStack(id);
        log.info("管理端删除技术栈成功，id={}", id);
        return HttpResult.success("技术栈删除成功");
    }

    /**
     * 批量删除技术栈（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    @ApiOperation(value = "批量删除技术栈")
    @DeleteMapping("/batch")
    @SaCheckPermission(value = "admin:techstack:delete", type = "admin")
    @AuditLog(action = "batch-delete", description = "批量删除技术栈，count=#{#batchDeleteDTO.techStackIds.size()}")
    public HttpResult batchDeleteTechStacks(@RequestBody @Valid @NotNull AdminTechStackBatchDeleteDTO batchDeleteDTO) {
        log.info("管理端批量删除技术栈请求，count={}", batchDeleteDTO.getTechStackIds().size());
        int deletedCount = adminTechStackService.batchDeleteTechStacks(batchDeleteDTO);
        log.info("管理端批量删除技术栈成功，deletedCount={}", deletedCount);
        return HttpResult.success("批量删除成功", deletedCount);
    }

    /**
     * 上传技术栈图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传技术栈图标")
    @PostMapping("/icon")
    @SaCheckPermission(value = "admin:techstack:add", type = "admin")
    @AuditLog(action = "upload-icon", description = "上传技术栈图标")
    public HttpResult uploadTechStackIcon(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("管理端上传技术栈图标请求，fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        AdminTechStackIconUploadVO uploadVO = adminTechStackService.uploadTechStackIcon(file);
        log.info("管理端上传技术栈图标成功，objectKey={}", uploadVO.getObjectKey());
        return HttpResult.success("上传成功", uploadVO);
    }

    /**
     * 删除技术栈图标
     *
     * @param objectKey 对象键
     * @return 执行结果
     */
    @ApiOperation(value = "删除技术栈图标")
    @DeleteMapping("/icon")
    @SaCheckPermission(value = "admin:techstack:add", type = "admin")
    @AuditLog(action = "delete-icon", description = "删除技术栈图标，objectKey=#{#objectKey}")
    public HttpResult deleteTechStackIcon(@RequestParam("objectKey") @NotNull String objectKey) {
        log.info("管理端删除技术栈图标请求，objectKey={}", objectKey);
        adminTechStackService.deleteTechStackIcon(objectKey);
        log.info("管理端删除技术栈图标成功，objectKey={}", objectKey);
        return HttpResult.success("删除成功");
    }
}
