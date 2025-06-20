package com.tebutebu.apiserver.global.errorcode;

import com.tebutebu.apiserver.global.constant.AuthErrorMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    INVALID_TOKEN(AuthErrorMessages.INVALID_TOKEN, HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(AuthErrorMessages.EXPIRED_TOKEN, HttpStatus.UNAUTHORIZED),
    MALFORMED_TOKEN(AuthErrorMessages.MALFORMED_TOKEN, HttpStatus.UNAUTHORIZED),
    INVALID_PROVIDER(AuthErrorMessages.INVALID_PROVIDER, HttpStatus.BAD_REQUEST),
    OAUTH_ALREADY_EXISTS(AuthErrorMessages.OAUTH_ALREADY_EXISTS, HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus status;

    AuthErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
