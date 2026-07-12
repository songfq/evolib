package com.evolib.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getErrorCode());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.fail(ErrorCode.INVALID_PARAM);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("无权限访问: {}", e.getMessage());
        Result<Void> result = new Result<>();
        result.setCode(403);
        result.setMessage("无权限访问");
        return result;
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        Result<Void> result = new Result<>();
        result.setCode(500);
        result.setMessage("系统内部错误");
        return result;
    }
}