package com.jady.appplatform.api.exception;

import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import com.jady.appplatform.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage());
    }

    // 参数校验失败：@Valid + @RequestBody + DTO上的@NotNull/@NotBlank
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Validation failed"
                : ex.getBindingResult().getFieldErrors().get(0).getField()
                + " " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException ex) {
        return ApiResponse.error("BAD_REQUEST", ex.getMessage());
    }

    // 唯一键/外键等约束冲突：典型就是重复注册 email
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException ex) {
        // 这里不直接把底层SQL错误暴露给前端，避免泄露内部细节
        return ApiResponse.error("DATA_CONFLICT", "Data conflict (possibly duplicated value).");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleServerError(Exception ex) {
        return ApiResponse.error("INTERNAL_ERROR", "Unexpected server error");
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException ex) {
        return ApiResponse.error("UNAUTHORIZED", ex.getMessage());
    }
}
