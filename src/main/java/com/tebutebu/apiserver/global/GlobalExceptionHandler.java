package com.tebutebu.apiserver.global;

import com.tebutebu.apiserver.util.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Map<String, String>> handleInvalidArgument(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("invalidRequest");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(CustomValidationException.class)
    protected ResponseEntity<Map<String, String>> handleValidationException(CustomValidationException e) {
        log.info("Validation failed: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CustomJWTException.class)
    protected ResponseEntity<Map<String, String>> handleJWTException(CustomJWTException e) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(CustomServiceException.class)
    protected ResponseEntity<Map<String, String>> handleServiceException(CustomServiceException e) {
        log.error("Service exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internalServerError");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        HttpStatus status = "invalidToken".equals(e.getMessage())
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;
        return build(status, e.getMessage());
    }

    /**
     * 정적 리소스를 찾을 수 없을 때 발생하는 예외 처리
     * 주로 루트 경로("/")나 존재하지 않는 정적 파일 요청 시 발생
     * 
     * @param e NoResourceFoundException
     * @return 404 응답과 안내 메시지
     */
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("정적 리소스를 찾을 수 없음: {}", e.getMessage());
        
        // API 서버이므로 정적 리소스 대신 API 안내 메시지 제공
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "message", "API 서버입니다. /health 또는 /info 엔드포인트를 사용해주세요.",
                    "endpoints", "/api/* (API 엔드포인트), /health (헬스체크), /info (서버 정보)",
                    "error", "staticResourceNotFound"
                ));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Map<String, String>> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internalServerError");
    }

    private ResponseEntity<Map<String, String>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }
}
