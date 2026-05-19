package com.yyyouth.web.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.web.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public HttpResult handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, msg={}", ex.getCode(), ex.getMessage());
        return HttpResult.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param ex 参数校验异常
     * @return 响应结果
     */
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResult handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "请求参数校验失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return HttpResult.error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 处理未登录异常
     *
     * @param ex 未登录异常
     * @return 响应结果
     */
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public HttpResult handleNotLoginException(NotLoginException ex) {
        log.warn("未登录访问: type={}", ex.getType());
        return HttpResult.error(AuthErrorCode.UNAUTHORIZED.getCode(), AuthErrorCode.UNAUTHORIZED.getMessage());
    }

    /**
     * 处理无权限异常
     *
     * @param ex 无权限异常
     * @return 响应结果
     */
    @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    @ExceptionHandler(NotPermissionException.class)
    public HttpResult handleNotPermissionException(NotPermissionException ex) {
        log.warn("无权限访问: permission={}", ex.getPermission());
        return HttpResult.error(AuthErrorCode.FORBIDDEN.getCode(), AuthErrorCode.FORBIDDEN.getMessage());
    }

    /**
     * 处理上传异常
     *
     * @param ex 上传异常
     * @return 响应结果
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public HttpResult handleUploadException(Exception ex) {
        log.warn("文件上传异常: {}", ex.getMessage());
        return HttpResult.error(HttpStatus.BAD_REQUEST, "上传文件失败，请检查文件格式和大小");
    }

    /**
     * 处理内容协商异常（如 produces 与 Accept 不匹配），回退为 JSON 返回
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
    public HttpResult handleMediaTypeNotAcceptableException(
            org.springframework.web.HttpMediaTypeNotAcceptableException ex) {
        log.warn("不支持的媒体类型: {}", ex.getMessage());
        return HttpResult.error(HttpStatus.BAD_REQUEST, "请求的响应格式不支持");
    }

    /**
     * 处理系统异常
     *
     * @param ex 系统异常
     * @return 响应结果
     */
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public HttpResult handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return HttpResult.error(HttpStatus.ERROR, ex.getMessage());
    }
}
