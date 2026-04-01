package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminWebsiteQueryDTO;
import com.yyyouth.model.vo.admin.AdminWebsiteCategoryVO;
import com.yyyouth.model.vo.admin.AdminWebsitePageVO;
import com.yyyouth.model.vo.admin.AdminWebsiteStatsVO;
import com.yyyouth.service.admin.website.AdminWebsiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
