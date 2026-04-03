package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminTagBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTagCreateDTO;
import com.yyyouth.model.dto.admin.AdminTagEditDTO;
import com.yyyouth.model.dto.admin.AdminTagQueryDTO;
import com.yyyouth.model.vo.admin.AdminTagPageVO;
import com.yyyouth.model.vo.admin.AdminTagStatsVO;
import com.yyyouth.service.admin.tag.AdminTagService;
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
 * @date 2026-04-03
 *
 * 管理端标签管理控制器
 */
@Slf4j
@Api(tags = "管理端标签管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tag")
public class AdminTagController {

    private final AdminTagService adminTagService;

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询标签分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:tag:list", type = "admin")
    public HttpResult list(@Valid AdminTagQueryDTO queryDTO) {
        log.info("查询管理端标签列表请求，pageNum={}, pageSize={}, keyword={}, sortField={}, sortOrder={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword(), queryDTO.getSortField(), queryDTO.getSortOrder());
        AdminTagPageVO pageVO = adminTagService.queryTagPage(queryDTO);
        log.info("查询管理端标签列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询标签统计
     *
     * @return 标签统计
     */
    @ApiOperation(value = "查询标签统计")
    @GetMapping("/stats")
    @SaCheckPermission(value = "admin:tag:stats", type = "admin")
    public HttpResult stats() {
        log.info("查询管理端标签统计请求");
        AdminTagStatsVO statsVO = adminTagService.queryTagStats();
        log.info("查询管理端标签统计成功，total={}", statsVO.getTotal());
        return HttpResult.success("查询成功", statsVO);
    }

    /**
     * 新增标签
     *
     * @param createDTO 创建参数
     * @return 标签ID
     */
    @ApiOperation(value = "新增标签")
    @PostMapping({"", "/"})
    @SaCheckPermission(value = "admin:tag:add", type = "admin")
    public HttpResult createTag(@RequestBody @Valid @NotNull AdminTagCreateDTO createDTO) {
        log.info("管理端新增标签请求，name={}, color={}", createDTO.getName(), createDTO.getColor());
        Long tagId = adminTagService.createTag(createDTO);
        log.info("管理端新增标签成功，tagId={}", tagId);
        return HttpResult.success("新增标签成功", tagId);
    }

    /**
     * 编辑标签
     *
     * @param id 标签ID
     * @param editDTO 编辑参数
     * @return 执行结果
     */
    @ApiOperation(value = "编辑标签")
    @PutMapping("/{id}")
    @SaCheckPermission(value = "admin:tag:edit", type = "admin")
    public HttpResult editTag(@PathVariable("id") @NotNull @Positive Long id,
                              @RequestBody @Valid @NotNull AdminTagEditDTO editDTO) {
        log.info("管理端编辑标签请求，id={}, name={}, color={}", id, editDTO.getName(), editDTO.getColor());
        adminTagService.editTag(id, editDTO);
        log.info("管理端编辑标签成功，id={}", id);
        return HttpResult.success("编辑标签成功");
    }

    /**
     * 删除标签（逻辑删除）
     *
     * @param id 标签ID
     * @return 执行结果
     */
    @ApiOperation(value = "删除标签")
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "admin:tag:delete", type = "admin")
    public HttpResult deleteTag(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端删除标签请求，id={}", id);
        adminTagService.deleteTag(id);
        log.info("管理端删除标签成功，id={}", id);
        return HttpResult.success("标签删除成功");
    }

    /**
     * 批量删除标签（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    @ApiOperation(value = "批量删除标签")
    @DeleteMapping("/batch")
    @SaCheckPermission(value = "admin:tag:delete", type = "admin")
    public HttpResult batchDeleteTags(@RequestBody @Valid @NotNull AdminTagBatchDeleteDTO batchDeleteDTO) {
        log.info("管理端批量删除标签请求，count={}", batchDeleteDTO.getTagIds().size());
        int deletedCount = adminTagService.batchDeleteTags(batchDeleteDTO);
        log.info("管理端批量删除标签成功，deletedCount={}", deletedCount);
        return HttpResult.success("批量删除成功", deletedCount);
    }
}
