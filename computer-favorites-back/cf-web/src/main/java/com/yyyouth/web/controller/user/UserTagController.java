package com.yyyouth.web.controller.user;

import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserTagQueryDTO;
import com.yyyouth.model.vo.user.UserTagPageVO;
import com.yyyouth.service.user.tag.UserTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签控制器
 */
@Slf4j
@Api(tags = "用户端标签接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class UserTagController {

    private final UserTagService userTagService;

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "查询标签分页列表")
    @GetMapping("/list")
    public HttpResult list(@Valid UserTagQueryDTO queryDTO) {
        log.info("用户端查询标签列表请求，pageNum={}, pageSize={}, keyword={}, sortField={}, sortOrder={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getKeyword(), queryDTO.getSortField(), queryDTO.getSortOrder());
        UserTagPageVO pageVO = userTagService.queryTagPage(queryDTO);
        log.info("用户端查询标签列表成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }
}