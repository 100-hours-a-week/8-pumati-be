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
    SNAPSHOT_LOCK_UNAVAILABLE(BusinessErrorMessages.SNAPSHOT_LOCK_UNAVAILABLE, HttpStatus.CONFLICT),
    SNAPSHOT_SERIALIZATION_FAILED(BusinessErrorMessages.SNAPSHOT_SERIALIZATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR),
    SNAPSHOT_LOCK_INTERRUPTED(BusinessErrorMessages.SNAPSHOT_LOCK_INTERRUPTED, HttpStatus.INTERNAL_SERVER_ERROR),
    SNAPSHOT_NOT_FOUND(BusinessErrorMessages.SNAPSHOT_NOT_FOUND, HttpStatus.NOT_FOUND),
    SNAPSHOT_ALREADY_IN_PROGRESS(BusinessErrorMessages.SNAPSHOT_ALREADY_IN_PROGRESS, HttpStatus.BAD_REQUEST),

    // Paging
    CONTEXT_ID_REQUIRED(BusinessErrorMessages.CONTEXT_ID_REQUIRED, HttpStatus.BAD_REQUEST),

    // Comment
    COMMENT_NOT_FOUND(BusinessErrorMessages.COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND),
    NOT_COMMENT_AUTHOR(BusinessErrorMessages.NOT_COMMENT_AUTHOR, HttpStatus.FORBIDDEN),
    NOT_AI_COMMENT(BusinessErrorMessages.NOT_AI_COMMENT, HttpStatus.BAD_REQUEST),

    // PreSignedUrl
    REQUEST_COUNT_EXCEEDED(BusinessErrorMessages.REQUEST_COUNT_EXCEEDED, HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION(BusinessErrorMessages.INVALID_FILE_EXTENSION, HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_EXTENSION(BusinessErrorMessages.UNSUPPORTED_FILE_EXTENSION, HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    BusinessErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
