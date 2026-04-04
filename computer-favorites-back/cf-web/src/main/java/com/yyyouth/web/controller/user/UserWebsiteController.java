package com.yyyouth.web.controller.user;

import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.service.user.website.UserWebsiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站资源控制器
 */
@Slf4j
@Api(tags = "用户端网站资源接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/website")
public class UserWebsiteController {

    private final UserWebsiteService userWebsiteService;

    /**
     * 查询网站分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "查询网站分页列表")
    @GetMapping("/list")
    public HttpResult list(@Valid UserWebsiteQueryDTO queryDTO) {
        log.info("用户端查询网站列表请求，pageNum={}, pageSize={}, categoryId={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getCategoryId());

        UserWebsitePageVO pageVO = userWebsiteService.queryWebsitePage(queryDTO);
        log.info("用户端查询网站列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询网站分类统计
     *
     * @return 分类统计
     */
    @ApiOperation(value = "查询网站分类统计")
    @GetMapping("/categories")
    public HttpResult categories() {
        log.info("用户端查询网站分类统计请求");
        List<UserWebsiteCategoryVO> categoryVOS = userWebsiteService.queryCategoryStats();
        log.info("用户端查询网站分类统计成功，size={}", categoryVOS.size());
        return HttpResult.success("查询成功", categoryVOS);
    }

    /**
     * 查询网站详情
     *
     * @param id 网站ID
     * @return 网站详情
     */
    @ApiOperation(value = "查询网站详情")
    @GetMapping("/{id}")
    public HttpResult detail(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("用户端查询网站详情请求，id={}", id);
        UserWebsiteDetailVO detailVO = userWebsiteService.queryWebsiteDetail(id);
        log.info("用户端查询网站详情成功，id={}", id);
        return HttpResult.success("查询成功", detailVO);
    }
}
