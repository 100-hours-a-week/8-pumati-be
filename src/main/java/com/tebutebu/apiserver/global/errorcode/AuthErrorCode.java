package com.tebutebu.apiserver.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    INVALID_TOKEN("invalidToken", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("expiredToken", HttpStatus.UNAUTHORIZED),
    MALFORMED_TOKEN("malformedToken", HttpStatus.UNAUTHORIZED),
    INVALID_PROVIDER("invalidProvider", HttpStatus.BAD_REQUEST),
    OAUTH_ALREADY_EXISTS("oauthAlreadyExists", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus status;

    AuthErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
