package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserAnnouncementQueryDTO;
import com.yyyouth.model.vo.user.UserAnnouncementDetailVO;
import com.yyyouth.model.vo.user.UserAnnouncementPageVO;
import com.yyyouth.service.user.announcement.UserAnnouncementService;
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

/**
 * @author yyyouth zg
 * @date 2026-04-23
 *
 * 用户端公告控制器
 */
@Slf4j
@Api(tags = "用户公告接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/announcements")
public class UserAnnouncementController {

    private final UserAnnouncementService userAnnouncementService;

    /**
     * 查询可见公告分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "查询可见公告分页列表")
    @GetMapping
    @SaCheckLogin
    public HttpResult queryAnnouncementPage(@Valid UserAnnouncementQueryDTO queryDTO) {
        log.info("收到用户公告分页查询请求，pageNum={}, pageSize={}, type={}, isTop={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getType(), queryDTO.getIsTop());
        UserAnnouncementPageVO pageVO = userAnnouncementService.queryAnnouncementPage(queryDTO);
        log.info("用户公告分页查询完成，total={}, totalPages={}", pageVO.getTotal(), pageVO.getTotalPages());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询可见公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @ApiOperation(value = "查询可见公告详情")
    @GetMapping("/{id}")
    @SaCheckLogin
    public HttpResult queryAnnouncementDetail(
            @PathVariable("id") @NotNull(message = "公告ID不能为空") @Positive(message = "公告ID必须为正数") Long id) {
        log.info("收到用户公告详情查询请求，id={}", id);
        UserAnnouncementDetailVO detailVO = userAnnouncementService.queryAnnouncementDetail(id);
        log.info("用户公告详情查询完成，id={}, title={}", id, detailVO.getTitle());
        return HttpResult.success("查询成功", detailVO);
    }
}
