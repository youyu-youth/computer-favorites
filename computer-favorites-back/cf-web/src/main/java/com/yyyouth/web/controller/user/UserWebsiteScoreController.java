package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.WebsiteScoreDTO;
import com.yyyouth.model.vo.user.WebsiteScoreMineVO;
import com.yyyouth.service.user.score.UserWebsiteScoreService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 用户网站评分接口控制器
 */
@Slf4j
@Api(tags = "用户网站评分接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/website")
public class UserWebsiteScoreController {

    private final UserWebsiteScoreService userWebsiteScoreService;

    /**
     * 网站评分
     *
     * @param id 网站ID
     * @param dto 评分参数
     * @return 操作结果
     */
    @ApiOperation(value = "网站评分")
    @PostMapping("/{id}/score")
    @SaCheckLogin
    public HttpResult score(@PathVariable @NotNull @Positive Long id,
                            @RequestBody @Valid @NotNull WebsiteScoreDTO dto) {
        log.info("收到网站评分请求，websiteId={}, score={}", id, dto.getScore());
        userWebsiteScoreService.score(id, dto.getScore());
        log.info("网站评分成功，websiteId={}, score={}", id, dto.getScore());
        return HttpResult.success("评分成功");
    }

    /**
     * 查询我的评分
     *
     * @param id 网站ID
     * @return 我的评分
     */
    @ApiOperation(value = "查询我的评分")
    @GetMapping("/{id}/score/mine")
    public HttpResult myScore(@PathVariable @NotNull @Positive Long id) {
        log.info("查询我的评分，websiteId={}", id);
        Integer myScore = userWebsiteScoreService.getMyScore(id);
        return HttpResult.success("查询成功",
                WebsiteScoreMineVO.builder().score(myScore).build());
    }
}
