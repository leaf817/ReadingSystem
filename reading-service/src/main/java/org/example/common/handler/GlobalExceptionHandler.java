package org.example.common.handler;


import lombok.extern.log4j.Log4j2;
import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.common.response.ResponseResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== 1. 业务异常处理 ==========
    @Order(Ordered.HIGHEST_PRECEDENCE)  // 最高优先级，最先匹配
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：code={}，message={}", e.getResponseCode().getCode(), e.getMessage());
        return ResponseResult.fail(e.getResponseCode(), e.getMessage());
    }

    // ========== 2. 认证异常处理（待实现）==========
    // @ExceptionHandler(AuthenticationException.class)
    // public ResponseResult<String> handleAuthenticationException(AuthenticationException e) {
    //     log.error("认证异常：{}", e.getMessage());
    //     return ResponseResult.fail(ResponseCode.UNAUTHORIZED, e.getMessage());
    // }

    // ========== 3. 参数校验异常处理（待实现）==========
    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseResult<String> handleValidationException(MethodArgumentNotValidException e) {
    //     log.error("参数校验异常：{}", e.getMessage());
    //     return ResponseResult.fail(ResponseCode.PARAM_ERROR, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    // }

    // ========== 4. 系统异常处理（兜底）==========
    @Order(Ordered.LOWEST_PRECEDENCE)   // 最低优先级，最后匹配
    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ResponseResult.fail(ResponseCode.SYSTEM_ERROR, e.getMessage());
    }
}