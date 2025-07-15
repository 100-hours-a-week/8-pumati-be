package com.tebutebu.apiserver.global.errorcode;

import com.tebutebu.apiserver.global.constant.BusinessErrorMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode implements ErrorCode {
    // Member
    MEMBER_NOT_FOUND(BusinessErrorMessages.MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(BusinessErrorMessages.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT),

    // Team
    TEAM_NOT_FOUND(BusinessErrorMessages.TEAM_NOT_FOUND, HttpStatus.NOT_FOUND),
    TEAM_ALREADY_EXISTS(BusinessErrorMessages.TEAM_ALREADY_EXISTS, HttpStatus.CONFLICT),
    NO_TEAM_NUMBERS_AVAILABLE(BusinessErrorMessages.NO_TEAM_NUMBERS_AVAILABLE, HttpStatus.NOT_FOUND),
    BADGE_MODIFICATION_IN_PROGRESS(BusinessErrorMessages.BADGE_MODIFICATION_IN_PROGRESS, HttpStatus.CONFLICT),

    // Project
    PROJECT_NOT_FOUND(BusinessErrorMessages.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND),
    PROJECT_ALREADY_EXISTS(BusinessErrorMessages.PROJECT_ALREADY_EXISTS, HttpStatus.CONFLICT),

    // Paging
    CONTEXT_ID_REQUIRED(BusinessErrorMessages.CONTEXT_ID_REQUIRED, HttpStatus.BAD_REQUEST),

    // Comment
    COMMENT_NOT_FOUND(BusinessErrorMessages.COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND),
    NOT_COMMENT_AUTHOR(BusinessErrorMessages.NOT_COMMENT_AUTHOR, HttpStatus.FORBIDDEN),
    NOT_AI_COMMENT(BusinessErrorMessages.NOT_AI_COMMENT, HttpStatus.BAD_REQUEST),

    // PreSignedUrl
    REQUEST_COUNT_EXCEEDED(BusinessErrorMessages.REQUEST_COUNT_EXCEEDED, HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION(BusinessErrorMessages.INVALID_FILE_EXTENSION, HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_EXTENSION(BusinessErrorMessages.UNSUPPORTED_FILE_EXTENSION, HttpStatus.BAD_REQUEST),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(BusinessErrorMessages.SUBSCRIPTION_NOT_FOUND, HttpStatus.NOT_FOUND),
    ALREADY_SUBSCRIBED(BusinessErrorMessages.ALREADY_SUBSCRIBED, HttpStatus.CONFLICT),

    // Mail
    MAIL_SEND_PROCESSING_FAILED(BusinessErrorMessages.MAIL_SEND_PROCESSING_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;

    BusinessErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
