package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserCollectCreateDTO;
import com.yyyouth.model.dto.user.UserCollectPageDTO;
import com.yyyouth.model.vo.user.UserCollectPageVO;
import com.yyyouth.model.vo.user.UserCollectStatsVO;
import com.yyyouth.service.user.collect.UserCollectService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏接口控制器
 */
@Slf4j
@Api(tags = "用户收藏接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/collect")
public class UserCollectController {

    private final UserCollectService userCollectService;

    /**
     * 收藏网站
     *
     * @param createDTO 收藏参数
     * @return 收藏条目ID
     */
    @ApiOperation(value = "收藏网站")
    @PostMapping
    @SaCheckLogin
    public HttpResult collect(@RequestBody @Valid @NotNull UserCollectCreateDTO createDTO) {
        log.info("收到收藏请求，websiteId={}, folderId={}", createDTO.getWebsiteId(), createDTO.getFolderId());
        Long collectId = userCollectService.collect(createDTO);
        log.info("收藏成功，collectId={}", collectId);
        return HttpResult.success("收藏成功", collectId);
    }

    /**
     * 取消收藏
     *
     * @param websiteId 网站ID
     * @return 操作结果
     */
    @ApiOperation(value = "取消收藏")
    @DeleteMapping("/{websiteId}")
    @SaCheckLogin
    public HttpResult cancelCollect(@PathVariable @NotNull @Positive Long websiteId) {
        log.info("收到取消收藏请求，websiteId={}", websiteId);
        userCollectService.cancelCollect(websiteId);
        log.info("取消收藏成功，websiteId={}", websiteId);
        return HttpResult.success("取消收藏成功");
    }

    /**
     * 分页查询收藏列表
     *
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    @ApiOperation(value = "分页查询收藏列表")
    @GetMapping("/list")
    @SaCheckLogin
    public HttpResult list(@Valid UserCollectPageDTO pageDTO) {
        log.info("收到收藏列表查询请求，folderId={}, pageNum={}, pageSize={}",
                pageDTO.getFolderId(), pageDTO.getPageNum(), pageDTO.getPageSize());
        UserCollectPageVO pageVO = userCollectService.pageCollectList(pageDTO);
        log.info("收藏列表查询成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 获取收藏统计
     *
     * @return 收藏统计
     */
    @ApiOperation(value = "获取收藏统计")
    @GetMapping("/stats")
    @SaCheckLogin
    public HttpResult stats() {
        log.info("收到收藏统计请求");
        UserCollectStatsVO statsVO = userCollectService.getCollectStats();
        log.info("收藏统计查询成功，collectCount={}, folderCount={}", statsVO.getCollectCount(), statsVO.getFolderCount());
        return HttpResult.success("查询成功", statsVO);
    }
}
