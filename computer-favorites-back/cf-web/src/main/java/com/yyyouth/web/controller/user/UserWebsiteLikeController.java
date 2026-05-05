package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.WebsiteLikePageDTO;
import com.yyyouth.model.vo.user.WebsiteLikePageVO;
import com.yyyouth.model.vo.user.WebsiteLikeStatusVO;
import com.yyyouth.service.user.like.UserWebsiteLikeService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站点赞接口控制器
 */
@Slf4j
@Api(tags = "用户网站点赞接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/website")
public class UserWebsiteLikeController {

    private final UserWebsiteLikeService userWebsiteLikeService;

    /**
     * 网站点赞
     *
     * @param id 网站ID
     * @return 操作结果
     */
    @ApiOperation(value = "网站点赞")
    @PostMapping("/{id}/like")
    @SaCheckLogin
    public HttpResult like(@PathVariable @NotNull @Positive Long id) {
        log.info("收到网站点赞请求，websiteId={}", id);
        userWebsiteLikeService.like(id);
        log.info("网站点赞成功，websiteId={}", id);
        return HttpResult.success("点赞成功");
    }

    /**
     * 取消点赞
     *
     * @param id 网站ID
     * @return 操作结果
     */
    @ApiOperation(value = "取消点赞")
    @DeleteMapping("/{id}/like")
    @SaCheckLogin
    public HttpResult unlike(@PathVariable @NotNull @Positive Long id) {
        log.info("收到取消网站点赞请求，websiteId={}", id);
        userWebsiteLikeService.unlike(id);
        log.info("取消网站点赞成功，websiteId={}", id);
        return HttpResult.success("取消点赞成功");
    }

    /**
     * 查询当前用户是否已点赞
     *
     * @param id 网站ID
     * @return 点赞状态
     */
    @ApiOperation(value = "查询当前用户是否已点赞")
    @GetMapping("/{id}/like/status")
    public HttpResult likeStatus(@PathVariable @NotNull @Positive Long id) {
        log.info("查询网站点赞状态，websiteId={}", id);
        Boolean isLiked = userWebsiteLikeService.isLiked(id);
        return HttpResult.success("查询成功",
                WebsiteLikeStatusVO.builder().isLiked(isLiked).build());
    }

    /**
     * 我点赞的网站列表
     *
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    @ApiOperation(value = "我点赞的网站列表")
    @GetMapping("/my/likes")
    @SaCheckLogin
    public HttpResult myLikes(@Valid WebsiteLikePageDTO pageDTO) {
        log.info("查询我点赞的网站列表，pageNum={}, pageSize={}", pageDTO.getPageNum(), pageDTO.getPageSize());
        WebsiteLikePageVO pageVO = userWebsiteLikeService.pageMyLikes(pageDTO);
        log.info("查询我点赞的网站列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }
}
