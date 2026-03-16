package com.yyyouth.common.exception;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 业务异常定义
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
