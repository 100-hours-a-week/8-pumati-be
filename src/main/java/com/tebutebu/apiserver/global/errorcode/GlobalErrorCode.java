package com.tebutebu.apiserver.global.errorcode;

import com.tebutebu.apiserver.global.constant.GlobalErrorMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(GlobalErrorMessages.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(GlobalErrorMessages.INVALID_REQUEST, HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    GlobalErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
