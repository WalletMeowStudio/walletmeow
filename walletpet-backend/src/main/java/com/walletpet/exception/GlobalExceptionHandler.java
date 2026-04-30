package com.walletpet.exception;

import com.walletpet.dto.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleUnauthorizedException(UnauthorizedException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        e.printStackTrace();
        return ApiResponse.fail("系統發生錯誤，請稍後再試");
    }
}