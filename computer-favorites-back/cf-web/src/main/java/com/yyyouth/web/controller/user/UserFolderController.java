package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.vo.user.UserFolderCreateVO;
import com.yyyouth.service.user.folder.UserFolderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏文件夹接口控制器
 */
@Slf4j
@Api(tags = "用户收藏文件夹接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/folder")
public class UserFolderController {

    private final UserFolderService userFolderService;

    /**
     * 创建收藏文件夹
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @ApiOperation(value = "创建收藏文件夹")
    @PostMapping
    @SaCheckLogin
    public HttpResult createFolder(@RequestBody @Valid @NotNull UserFolderCreateDTO createDTO) {
        log.info("收到创建收藏夹请求，name={}, parentId={}, sort={}",
                createDTO.getName(), createDTO.getParentId(), createDTO.getSort());
        UserFolderCreateVO result = userFolderService.createFolder(createDTO);
        log.info("创建收藏夹成功，folderId={}, name={}", result.getId(), result.getName());
        return HttpResult.success("创建成功", result);
    }
}
