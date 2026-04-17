package com.yyyouth.web.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.user.UserFeedbackCreateDTO;
import com.yyyouth.model.vo.user.UserFeedbackImageUploadVO;
import com.yyyouth.service.user.notification.UserFeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈接口控制器
 */
@Slf4j
@Api(tags = "用户反馈接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/feedbacks")
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 上传反馈图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传反馈图片")
    @PostMapping("/images")
    @SaCheckLogin
    public HttpResult uploadFeedbackImage(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("收到用户反馈图片上传请求，fileName={}, size={}",
                file.getOriginalFilename(), file.getSize());
        UserFeedbackImageUploadVO uploadVO = userFeedbackService.uploadFeedbackImage(file);
        log.info("用户反馈图片上传成功，objectKey={}", uploadVO.getObjectKey());
        return HttpResult.success("上传成功", uploadVO);
    }

    /**
     * 删除反馈图片
     *
     * @param objectKey 对象键
     * @return 删除结果
     */
    @ApiOperation(value = "删除反馈图片")
    @DeleteMapping("/images")
    @SaCheckLogin
    public HttpResult deleteFeedbackImage(@RequestParam("objectKey") @NotBlank(message = "对象键不能为空") String objectKey) {
        log.info("收到用户反馈图片删除请求，objectKey={}", objectKey);
        userFeedbackService.deleteFeedbackImage(objectKey);
        log.info("用户反馈图片删除成功，objectKey={}", objectKey);
        return HttpResult.success("删除成功");
    }

    /**
     * 提交用户反馈
     *
     * @param createDTO 反馈参数
     * @return 执行结果
     */
    @ApiOperation(value = "提交用户反馈")
    @PostMapping
    @SaCheckLogin
    public HttpResult submitFeedback(@RequestBody @Valid @NotNull UserFeedbackCreateDTO createDTO) {
        log.info("收到用户反馈提交请求，type={}, imageCount={}",
                createDTO.getType(), createDTO.getImages() == null ? 0 : createDTO.getImages().size());
        userFeedbackService.submitFeedback(createDTO);
        log.info("用户反馈提交成功，type={}", createDTO.getType());
        return HttpResult.success("反馈提交成功");
    }
}
