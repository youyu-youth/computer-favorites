package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminWebsiteBatchStatusUpdateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteCreateDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteStatusUpdateDTO;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsiteDetailVO;
import com.yyyouth.model.vo.admin.AdminWebsiteLogoUploadVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-01
 *
 * 管理端网站治理控制器
 */
@Slf4j
@Api(tags = "管理端网站治理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/website")
public class AdminWebsiteController {

    private final AdminWebsiteService adminWebsiteService;

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @ApiOperation(value = "查询网站分页列表")
    @GetMapping("/list")
    @SaCheckPermission(value = "admin:website:list", type = "admin")
    public HttpResult list(@Valid AdminWebsiteQueryDTO queryDTO) {
        log.info("查询管理端网站列表请求，pageNum={}, pageSize={}, deleted={}, categoryId={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getDeleted(), queryDTO.getCategoryId());
        AdminWebsitePageVO pageVO = adminWebsiteService.queryWebsitePage(queryDTO);
        log.info("查询管理端网站列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询网站详情
     *
     * @param id 网站ID
     * @return 网站详情
     */
    @ApiOperation(value = "查询网站详情")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "admin:website:detail", type = "admin")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("查询管理端网站详情请求，id={}", id);
        AdminWebsiteDetailVO detailVO = adminWebsiteService.queryWebsiteDetail(id);
        log.info("查询管理端网站详情成功，id={}", id);
        return HttpResult.success("查询成功", detailVO);
    }

    /**
     * 查询网站分类统计
     *
     * @param deleted 删除筛选
     * @return 分类统计
     */
    @ApiOperation(value = "查询网站分类统计")
    @GetMapping("/categories")
    @SaCheckPermission(value = "admin:website:category:list", type = "admin")
    public HttpResult categories(@RequestParam(required = false) Integer deleted) {
        log.info("查询管理端网站分类统计请求，deleted={}", deleted);
        List<AdminWebsiteCategoryVO> categoryVOS = adminWebsiteService.queryCategoryStats(deleted);
        log.info("查询管理端网站分类统计成功，size={}", categoryVOS.size());
        return HttpResult.success("查询成功", categoryVOS);
    }

    /**
     * 查询网站统计信息
     *
     * @param deleted 删除筛选
     * @return 统计信息
     */
    @ApiOperation(value = "查询网站统计信息")
    @GetMapping("/stats")
    @SaCheckPermission(value = "admin:website:stats", type = "admin")
    public HttpResult stats(@RequestParam(required = false) Integer deleted) {
        log.info("查询管理端网站统计请求，deleted={}", deleted);
        AdminWebsiteStatsVO statsVO = adminWebsiteService.queryWebsiteStats(deleted);
        log.info("查询管理端网站统计成功，total={}", statsVO.getTotal());
        return HttpResult.success("查询成功", statsVO);
    }

    /**
     * 上传网站 Logo
     *
     * @param file Logo 文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传网站Logo")
    @PostMapping("/logo")
    @SaCheckPermission(value = "admin:website:add", type = "admin")
    public HttpResult uploadLogo(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("管理端上传网站Logo请求，fileName={}, size={}", file == null ? "" : file.getOriginalFilename(), file == null ? 0 : file.getSize());
        AdminWebsiteLogoUploadVO uploadVO = adminWebsiteService.uploadWebsiteLogo(file);
        log.info("管理端上传网站Logo成功，objectKey={}", uploadVO.getObjectKey());
        return HttpResult.success("上传成功", uploadVO);
    }

    /**
     * 删除网站 Logo
     *
     * @param objectKey 对象键
     * @return 删除结果
     */
    @ApiOperation(value = "删除网站Logo")
    @DeleteMapping("/logo")
    @SaCheckPermission(value = "admin:website:add", type = "admin")
    public HttpResult deleteLogo(@RequestParam("objectKey") @NotBlank(message = "对象键不能为空") String objectKey) {
        log.info("管理端删除网站Logo请求，objectKey={}", objectKey);
        adminWebsiteService.deleteWebsiteLogo(objectKey);
        log.info("管理端删除网站Logo成功，objectKey={}", objectKey);
        return HttpResult.success("删除成功");
    }

    /**
     * 新增网站
     *
     * @param createDTO 新增参数
     * @return 新增结果
     */
    @ApiOperation(value = "新增网站")
    @PostMapping({"", "/"})
    @SaCheckPermission(value = "admin:website:add", type = "admin")
    public HttpResult createWebsite(@RequestBody @Valid @NotNull AdminWebsiteCreateDTO createDTO) {
        log.info("管理端新增网站请求，name={}, categoryId={}", createDTO.getName(), createDTO.getCategoryId());
        Long websiteId = adminWebsiteService.createWebsite(createDTO);
        log.info("管理端新增网站成功，websiteId={}", websiteId);
        return HttpResult.success("添加网站成功", websiteId);
    }

    /**
     * 更新网站上架状态
     *
     * @param updateDTO 状态更新参数
     * @return 操作结果
     */
    @ApiOperation(value = "更新网站上架状态")
    @PutMapping("/status")
    @SaCheckPermission(value = "admin:website:status", type = "admin")
    public HttpResult updateWebsiteStatus(@RequestBody @Valid @NotNull AdminWebsiteStatusUpdateDTO updateDTO) {
        log.info("管理端更新网站状态请求，websiteId={}, status={}", updateDTO.getWebsiteId(), updateDTO.getStatus());
        adminWebsiteService.updateWebsiteStatus(updateDTO);
        log.info("管理端更新网站状态成功，websiteId={}, status={}", updateDTO.getWebsiteId(), updateDTO.getStatus());
        return HttpResult.success("网站状态更新成功");
    }

    /**
     * 批量更新网站上架状态
     *
     * @param updateDTO 批量状态更新参数
     * @return 操作结果
     */
    @ApiOperation(value = "批量更新网站上架状态")
    @PutMapping("/status/batch")
    @SaCheckPermission(value = "admin:website:status", type = "admin")
    public HttpResult batchUpdateWebsiteStatus(@RequestBody @Valid @NotNull AdminWebsiteBatchStatusUpdateDTO updateDTO) {
        log.info("管理端批量更新网站状态请求，count={}, status={}", updateDTO.getWebsiteIds().size(), updateDTO.getStatus());
        int updatedCount = adminWebsiteService.batchUpdateWebsiteStatus(updateDTO);
        log.info("管理端批量更新网站状态成功，updatedCount={}, status={}", updatedCount, updateDTO.getStatus());
        return HttpResult.success("批量状态更新成功", updatedCount);
    }
}
