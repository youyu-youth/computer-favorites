package com.yyyouth.web.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.common.web.HttpResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 全局异常处理器
 */
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
        return HttpResult.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param ex 参数校验异常
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResult handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "请求参数校验失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        return HttpResult.error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 处理未登录异常
     *
     * @param ex 未登录异常
     * @return 响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public HttpResult handleNotLoginException(NotLoginException ex) {
        return HttpResult.error(AuthErrorCode.UNAUTHORIZED.getCode(), AuthErrorCode.UNAUTHORIZED.getMessage());
    }

    /**
     * 处理无权限异常
     *
     * @param ex 无权限异常
     * @return 响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    public HttpResult handleNotPermissionException(NotPermissionException ex) {
        return HttpResult.error(AuthErrorCode.FORBIDDEN.getCode(), AuthErrorCode.FORBIDDEN.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param ex 系统异常
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    public HttpResult handleException(Exception ex) {
        return HttpResult.error(HttpStatus.ERROR, ex.getMessage());
    }
}
