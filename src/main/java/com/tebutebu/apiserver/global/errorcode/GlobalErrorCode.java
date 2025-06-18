package com.tebutebu.apiserver.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR("internalServerError", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("invalidRequest", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    GlobalErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
