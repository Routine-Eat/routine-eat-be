package com.likelion.routineeatbe.global.exception;
import com.likelion.routineeatbe.global.enums.GlobalErrorCode;
import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse<Object>> handleCustomException(CustomException ex) {
        BaseErrorCode errorCode = ex.getErrorCode();
        log.warn("CustomException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(GlobalResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errorMessages =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.joining(" / "));
        log.warn("Validation 오류 발생: {}", errorMessages);
        return ResponseEntity.badRequest().body(GlobalResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE.getCode(), GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<GlobalResponse<?>> handleBindException(BindException ex) {
        log.warn("요청 파라미터 검증 오류 발생: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(GlobalResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalResponse<?>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        log.warn("요청 파라미터 타입 오류 발생: {}", ex.getName());
        return ResponseEntity.badRequest().body(GlobalResponse.error(
                GlobalErrorCode.INVALID_TYPE_VALUE.getCode(),
                GlobalErrorCode.INVALID_TYPE_VALUE.getMessage()
        ));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<GlobalResponse<?>> handleRequestParameterValidationException(
            Exception ex
    ) {
        log.warn("요청 파라미터 검증 오류 발생: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(GlobalResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()
        ));
    }

    // 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<?>> handleException(Exception ex) {
        log.error("Server 오류 발생: ", ex);
        return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(GlobalResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(), GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}


