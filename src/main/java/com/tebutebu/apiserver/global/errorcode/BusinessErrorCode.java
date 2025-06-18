package com.tebutebu.apiserver.global.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode implements ErrorCode {
    // Member
    MEMBER_NOT_FOUND("memberNotFound", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("emailAlreadyExists", HttpStatus.CONFLICT),

    // Team
    TEAM_NOT_FOUND("teamNotFound", HttpStatus.NOT_FOUND),
    TEAM_ALREADY_EXISTS("teamAlreadyExists", HttpStatus.CONFLICT),
    NO_TEAM_NUMBERS_AVAILABLE("noTeamNumbersAvailable", HttpStatus.NOT_FOUND),
    BADGE_MODIFICATION_IN_PROGRESS("badgeModificationInProgress", HttpStatus.CONFLICT),

    // Project
    PROJECT_NOT_FOUND("projectNotFound", HttpStatus.NOT_FOUND),
    PROJECT_ALREADY_EXISTS("projectAlreadyExists", HttpStatus.CONFLICT),

    // Paging
    CONTEXT_ID_REQUIRED("contextIdRequired", HttpStatus.BAD_REQUEST),

    // Comment
    COMMENT_NOT_FOUND("commentNotFound", HttpStatus.NOT_FOUND),
    NOT_COMMENT_AUTHOR("notCommentAuthor", HttpStatus.FORBIDDEN),
    NOT_AI_COMMENT("notAiComment", HttpStatus.BAD_REQUEST),

    // PreSignedUrl
    REQUEST_COUNT_EXCEEDED("requestCountExceeded", HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION("invalidFileExtension", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_EXTENSION("unsupportedFileExtension", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    BusinessErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
