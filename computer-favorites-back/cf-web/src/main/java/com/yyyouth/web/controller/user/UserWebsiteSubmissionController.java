package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionQueryDTO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionIconUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionPageVO;
import com.yyyouth.service.user.website.UserWebsiteSubmissionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿网站控制器
 */
@Slf4j
@Api(tags = "用户投稿网站接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/website/submission")
public class UserWebsiteSubmissionController {

    private final UserWebsiteSubmissionService userWebsiteSubmissionService;

    /**
     * 上传投稿网站图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传投稿网站图标")
    @PostMapping("/icon")
    @SaCheckPermission("user:website:submit")
    public HttpResult uploadSubmissionIcon(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("用户上传投稿图标请求，fileName={}, size={}", file == null ? "" : file.getOriginalFilename(), file == null ? 0 : file.getSize());
        UserWebsiteSubmissionIconUploadVO uploadVO = userWebsiteSubmissionService.uploadSubmissionIcon(file);
        log.info("用户上传投稿图标成功，objectKey={}", uploadVO.getObjectKey());
        return HttpResult.success("上传成功", uploadVO);
    }

    /**
     * 删除投稿网站图标
     *
     * @param objectKey 对象键
     * @return 删除结果
     */
    @ApiOperation(value = "删除投稿网站图标")
    @DeleteMapping("/icon")
    @SaCheckPermission("user:website:submit")
    public HttpResult deleteSubmissionIcon(@RequestParam("objectKey") @NotBlank(message = "对象键不能为空") String objectKey) {
        log.info("用户删除投稿图标请求，objectKey={}", objectKey);
        userWebsiteSubmissionService.deleteSubmissionIcon(objectKey);
        log.info("用户删除投稿图标成功，objectKey={}", objectKey);
        return HttpResult.success("删除成功");
    }

    /**
     * 提交网站投稿
     *
     * @param createDTO 投稿参数
     * @return 投稿网站ID
     */
    @ApiOperation(value = "提交网站投稿")
    @PostMapping
    @SaCheckPermission("user:website:submit")
    public HttpResult submitWebsite(@RequestBody @Valid @NotNull UserWebsiteSubmissionCreateDTO createDTO) {
        log.info("用户提交网站投稿请求，name={}, categoryId={}", createDTO.getName(), createDTO.getCategoryId());
        Long websiteId = userWebsiteSubmissionService.submitWebsite(createDTO);
        log.info("用户提交网站投稿成功，websiteId={}", websiteId);
        return HttpResult.success("投稿提交成功", websiteId);
    }

    /**
     * 查询我的投稿分页
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @ApiOperation(value = "查询我的投稿分页")
    @GetMapping("/list")
    @SaCheckPermission("user:website:list")
    public HttpResult queryMySubmissionPage(@Valid UserWebsiteSubmissionQueryDTO queryDTO) {
        log.info("用户查询投稿分页请求，pageNum={}, pageSize={}, auditStatus={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getAuditStatus());
        UserWebsiteSubmissionPageVO pageVO = userWebsiteSubmissionService.queryMySubmissionPage(queryDTO);
        log.info("用户查询投稿分页成功，total={}", pageVO.getTotal());
        return HttpResult.success("查询成功", pageVO);
    }

    /**
     * 查询我的投稿详情
     *
     * @param websiteId 网站ID
     * @return 投稿详情
     */
    @ApiOperation(value = "查询我的投稿详情")
    @GetMapping("/{websiteId}")
    @SaCheckPermission("user:website:detail")
    public HttpResult queryMySubmissionDetail(@PathVariable("websiteId") @NotNull @Positive Long websiteId) {
        log.info("用户查询投稿详情请求，websiteId={}", websiteId);
        UserWebsiteSubmissionDetailVO detailVO = userWebsiteSubmissionService.queryMySubmissionDetail(websiteId);
        log.info("用户查询投稿详情成功，websiteId={}", websiteId);
        return HttpResult.success("查询成功", detailVO);
    }

    /**
     * 编辑我的投稿
     *
     * @param websiteId 网站ID
     * @param editDTO 编辑参数
     * @return 操作结果
     */
    @ApiOperation(value = "编辑我的投稿")
    @PutMapping("/{websiteId}")
    @SaCheckPermission("user:website:edit")
    public HttpResult editMySubmission(@PathVariable("websiteId") @NotNull @Positive Long websiteId,
                                       @RequestBody @Valid @NotNull UserWebsiteSubmissionCreateDTO editDTO) {
        log.info("用户编辑投稿请求，websiteId={}", websiteId);
        userWebsiteSubmissionService.editMySubmission(websiteId, editDTO);
        log.info("用户编辑投稿成功，websiteId={}", websiteId);
        return HttpResult.success("投稿更新成功");
    }

    /**
     * 取消我的投稿
     *
     * @param websiteId 网站ID
     * @return 操作结果
     */
    @ApiOperation(value = "取消我的投稿")
    @DeleteMapping("/{websiteId}")
    @SaCheckPermission("user:website:cancel")
    public HttpResult cancelMySubmission(@PathVariable("websiteId") @NotNull @Positive Long websiteId) {
        log.info("用户取消投稿请求，websiteId={}", websiteId);
        userWebsiteSubmissionService.cancelMySubmission(websiteId);
        log.info("用户取消投稿成功，websiteId={}", websiteId);
        return HttpResult.success("投稿取消成功");
    }

    /**
     * 重新提交我的投稿
     *
     * @param websiteId 网站ID
     * @return 操作结果
     */
    @ApiOperation(value = "重新提交我的投稿")
    @PutMapping("/{websiteId}/resubmit")
    @SaCheckPermission("user:website:resubmit")
    public HttpResult resubmitMySubmission(@PathVariable("websiteId") @NotNull @Positive Long websiteId) {
        log.info("用户重新提交投稿请求，websiteId={}", websiteId);
        userWebsiteSubmissionService.resubmitMySubmission(websiteId);
        log.info("用户重新提交投稿成功，websiteId={}", websiteId);
        return HttpResult.success("重新提交成功");
    }
}
