package com.tebutebu.apiserver.global.constant;

public class BusinessErrorMessages {

    private BusinessErrorMessages() {}

    // Member
    public static final String MEMBER_NOT_FOUND = "memberNotFound";
    public static final String EMAIL_ALREADY_EXISTS = "emailAlreadyExists";

    // Team
    public static final String TEAM_NOT_FOUND = "teamNotFound";
    public static final String TEAM_ALREADY_EXISTS = "teamAlreadyExists";
    public static final String NO_TEAM_NUMBERS_AVAILABLE = "noTeamNumbersAvailable";
    public static final String BADGE_MODIFICATION_IN_PROGRESS = "badgeModificationInProgress";

    // Project
    public static final String PROJECT_NOT_FOUND = "projectNotFound";
    public static final String PROJECT_ALREADY_EXISTS = "projectAlreadyExists";
    public static final String SNAPSHOT_LOCK_UNAVAILABLE = "snapshotLockUnavailable";
    public static final String SNAPSHOT_SERIALIZATION_FAILED = "snapshotSerializationFailed";
    public static final String SNAPSHOT_LOCK_INTERRUPTED = "snapshotLockInterrupted";
    public static final String SNAPSHOT_NOT_FOUND = "snapshotNotFound";
    public static final String SNAPSHOT_ALREADY_IN_PROGRESS = "snapshotAlreadyInProgress";

    // Paging
    public static final String CONTEXT_ID_REQUIRED = "contextIdRequired";

    // Comment
    public static final String COMMENT_NOT_FOUND = "commentNotFound";
    public static final String NOT_COMMENT_AUTHOR = "notCommentAuthor";
    public static final String NOT_AI_COMMENT = "notAiComment";

    // PreSignedUrl
    public static final String REQUEST_COUNT_EXCEEDED = "requestCountExceeded";
    public static final String INVALID_FILE_EXTENSION = "invalidFileExtension";
    public static final String UNSUPPORTED_FILE_EXTENSION = "unsupportedFileExtension";

    // RefreshToken
    public static final String INVALID_REFRESH_TOKEN = "invalidRefreshToken";
    public static final String EXPIRED_REFRESH_TOKEN = "expiredRefreshToken";
    public static final String REFRESH_TOKEN_NOT_FOUND = "refreshTokenNotFound";
    public static final String INVALID_OR_EXPIRED_REFRESH_TOKEN = "invalidOrExpiredRefreshToken";

}
