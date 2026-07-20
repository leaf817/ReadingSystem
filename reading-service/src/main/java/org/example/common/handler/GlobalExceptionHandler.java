package org.example.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.common.response.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}，message={}", e.getResponseCode().getCode(), e.getMessage());
        return ResponseResult.fail(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ResponseResult.fail(ResponseCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}
