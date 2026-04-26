package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.PasswordVerifyDTO;
import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.dto.user.UserFolderUpdateDTO;
import com.yyyouth.model.vo.user.UserFolderCreateVO;
import com.yyyouth.model.vo.user.UserFolderOptionsVO;
import com.yyyouth.model.vo.user.UserFolderTreeVO;
import com.yyyouth.service.user.folder.UserFolderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 获取文件夹树
     *
     * @return 文件夹树列表
     */
    @ApiOperation(value = "获取文件夹树")
    @GetMapping("/tree")
    @SaCheckLogin
    public HttpResult tree() {
        log.info("收到文件夹树查询请求");
        List<UserFolderTreeVO> tree = userFolderService.getFolderTree();
        log.info("文件夹树查询成功，size={}", tree.size());
        return HttpResult.success("查询成功", tree);
    }

    /**
     * 更新收藏文件夹
     *
     * @param id 文件夹ID
     * @param updateDTO 更新参数
     * @return 操作结果
     */
    @ApiOperation(value = "更新收藏文件夹")
    @PutMapping("/{id}")
    @SaCheckLogin
    public HttpResult updateFolder(@PathVariable @NotNull @Positive Long id,
                                   @RequestBody @Valid UserFolderUpdateDTO updateDTO) {
        log.info("收到更新收藏夹请求，id={}", id);
        userFolderService.updateFolder(id, updateDTO);
        log.info("更新收藏夹成功，id={}", id);
        return HttpResult.success("更新成功");
    }

    /**
     * 删除收藏文件夹
     *
     * @param id 文件夹ID
     * @return 操作结果
     */
    @ApiOperation(value = "删除收藏文件夹")
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public HttpResult deleteFolder(@PathVariable @NotNull @Positive Long id) {
        log.info("收到删除收藏夹请求，id={}", id);
        userFolderService.deleteFolder(id);
        log.info("删除收藏夹成功，id={}", id);
        return HttpResult.success("删除成功");
    }

    /**
     * 切换文件夹隐藏状态
     *
     * @param id 文件夹ID
     * @param isHide 是否隐藏
     * @return 操作结果
     */
    @ApiOperation(value = "切换文件夹隐藏状态")
    @PutMapping("/{id}/hide")
    @SaCheckLogin
    public HttpResult toggleHide(@PathVariable @NotNull @Positive Long id,
                                 @RequestParam boolean isHide) {
        log.info("收到文件夹隐藏切换请求，id={}, isHide={}", id, isHide);
        userFolderService.toggleFolderHide(id, isHide);
        log.info("文件夹隐藏切换成功，id={}, isHide={}", id, isHide);
        return HttpResult.success(isHide ? "隐藏成功" : "显示成功");
    }

    /**
     * 获取文件夹下拉选项
     *
     * @return 文件夹选项列表
     */
    @ApiOperation(value = "获取文件夹下拉选项")
    @GetMapping("/options")
    @SaCheckLogin
    public HttpResult options() {
        log.info("收到文件夹选项查询请求");
        List<UserFolderOptionsVO> options = userFolderService.getFolderOptions();
        log.info("文件夹选项查询成功，size={}", options.size());
        return HttpResult.success("查询成功", options);
    }

    /**
     * 验证用户密码
     *
     * @param verifyDTO 密码验证参数
     * @return 验证结果
     */
    @ApiOperation(value = "验证用户密码")
    @PostMapping("/verify-password")
    @SaCheckLogin
    public HttpResult verifyPassword(@RequestBody @Valid @NotNull PasswordVerifyDTO verifyDTO) {
        log.info("收到密码验证请求");
        boolean matched = userFolderService.verifyPassword(verifyDTO.getPassword());
        log.info("密码验证结果，matched={}", matched);
        if (matched) {
            return HttpResult.success("验证通过", true);
        }
        return HttpResult.error(400, "密码错误");
    }
}
