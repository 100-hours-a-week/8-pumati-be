package com.tebutebu.apiserver.controller.advice;

import com.tebutebu.apiserver.util.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Log4j2
public class CustomControllerAdvice {

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

    @ExceptionHandler(CustomAuthenticationException.class)
    protected ResponseEntity<Map<String, String>> handleAuthenticationException(CustomAuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "invalidCredentials");
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
