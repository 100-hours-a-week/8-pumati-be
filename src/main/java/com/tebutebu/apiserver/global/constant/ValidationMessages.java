package com.tebutebu.apiserver.global.constant;

public class ValidationMessages {

    private ValidationMessages() {}

    // Member
    public static final String MEMBER_ID_REQUIRED = "memberIdRequired";
    public static final String MEMBER_ID_MUST_BE_POSITIVE = "memberIdMustBePositive";
    public static final String MEMBER_NAME_REQUIRED = "memberNameRequired";
    public static final String MEMBER_NAME_MAX_LENGTH_EXCEEDED = "memberNameMaxLengthExceeded";
    public static final String MEMBER_NICKNAME_REQUIRED = "memberNicknameRequired";
    public static final String MEMBER_NICKNAME_MAX_LENGTH_EXCEEDED = "memberNicknameMaxLengthExceeded";
    public static final String MEMBER_NICKNAME_VIOLATED = "memberNicknameViolated";

    // Team
    public static final String TEAM_ID_REQUIRED = "teamIdRequired";
    public static final String TEAM_TERM_REQUIRED = "teamTermRequired";
    public static final String TERM_MUST_BE_POSITIVE = "termMustBePositive";
    public static final String TEAM_NUMBER_REQUIRED = "teamNumberRequired";
    public static final String TEAM_NUMBER_MUST_BE_POSITIVE = "teamNumberMustBePositive";

    // Team Badge
    public static final String BADGE_IMAGE_URL_REQUIRED = "badgeImageUrlRequired";
    public static final String BADGE_IMAGE_URL_MAX_LENGTH_EXCEEDED = "badgeImageUrlMaxLengthExceeded";

    // Project
    public static final String PROJECT_ID_REQUIRED = "projectIdRequired";
    public static final String PROJECT_ID_MUST_BE_POSITIVE = "projectIdMustBePositive";
    public static final String PROJECT_TITLE_REQUIRED = "titleRequired";
    public static final String PROJECT_TITLE_MAX_LENGTH_EXCEEDED = "titleMaxLengthExceeded";
    public static final String PROJECT_INTRODUCTION_MAX_LENGTH_EXCEEDED = "introductionMaxLengthExceeded";
    public static final String PROJECT_DETAILED_DESCRIPTION_MAX_LENGTH_EXCEEDED = "detailedDescriptionMaxLengthExceeded";
    public static final String PROJECT_DEPLOYMENT_URL_MAX_LENGTH_EXCEEDED = "deploymentUrlMaxLengthExceeded";
    public static final String PROJECT_GITHUB_URL_MAX_LENGTH_EXCEEDED = "githubUrlMaxLengthExceeded";
    public static final String PROJECT_TAGS_REQUIRED = "tagsRequired";
    public static final String PROJECT_TAGS_SIZE_OUT_OF_BOUNDS = "tagsSizeOutOfBounds";
    public static final String PROJECT_IMAGES_REQUIRED = "imagesRequired";
    public static final String PROJECT_IMAGES_MIN_SIZE_REQUIRED = "imagesMinSizeRequired";
    public static final String PROJECT_IMAGES_SIZE_VIOLATED = "projectImagesMinSizeViolated";

    // Project Image
    public static final String PROJECT_IMAGE_URL_REQUIRED = "projectImageUrlRequired";
    public static final String PROJECT_IMAGE_URL_MAX_LENGTH_EXCEEDED = "projectImageUrlMaxLengthExceeded";
    public static final String PROJECT_IMAGE_SEQUENCE_REQUIRED = "projectImageSequenceRequired";

    // Tag
    public static final String TAG_CONTENT_REQUIRED = "tagContentRequired";
    public static final String TAG_CONTENT_MAX_LENGTH_EXCEEDED = "tagContentMaxLengthExceeded";
    public static final String TAG_CONTENT_VIOLATED = "tagContentMustNotContainWhitespace";
    public static final String EACH_TAG_MUST_NOT_BE_BLANK = "eachTagMustNotBeBlank";

    // Token
    public static final String SIGNUP_TOKEN_REQUIRED = "signupTokenRequired";
    public static final String TOKEN_REQUIRED = "tokenRequired";
    public static final String OLD_TOKEN_REQUIRED = "oldTokenRequired";
    public static final String NEW_EXPIRY_MINUTES_REQUIRED = "newExpiryMinutesRequired";
    public static final String NEW_EXPIRY_MINUTES_MUST_BE_POSITIVE = "newExpiryMinutesMustBePositive";
    public static final String EXPIRES_AT_REQUIRED = "expiresAtRequired";

    // Comment
    public static final String COMMENT_CONTENT_REQUIRED = "commentContentRequired";
    public static final String COMMENT_CONTENT_MAX_LENGTH_EXCEEDED = "commentContentMaxLengthExceeded";

    // AI Comment
    public static final String AI_COMMENT_AUTHOR_NAME_REQUIRED = "aiCommentAuthorNameRequired";
    public static final String AI_COMMENT_AUTHOR_NICKNAME_REQUIRED = "aiCommentAuthorNicknameRequired";
    public static final String AI_COMMENT_NICKNAME_REQUIRED = "aiCommentNicknameRequired";
    public static final String AI_COMMENT_NICKNAME_MAX_LENGTH_EXCEEDED = "aiCommentNicknameMaxLengthExceeded";

    // Chatbot
    public static final String CHATBOT_CONTENT_REQUIRED = "chatbotContentRequired";

    // Provider / OAuth
    public static final String PROVIDER_REQUIRED = "providerRequired";
    public static final String PROVIDER_ID_REQUIRED = "providerIdRequired";
    public static final String PROVIDER_MAX_LENGTH_EXCEEDED = "providerMaxLengthExceeded";
    public static final String PROVIDER_ID_MAX_LENGTH_EXCEEDED = "providerIdMaxLengthExceeded";

    // Pagination
    public static final String PAGE_SIZE_REQUIRED = "pageSizeRequired";
    public static final String PAGE_SIZE_MIN = "pageSizeMin";
    public static final String PAGE_SIZE_MAX = "pageSizeMax";
    public static final String CURSOR_ID_MUST_BE_POSITIVE = "cursorIdMustBePositive";
    public static final String CURSOR_COUNT_MUST_BE_POSITIVE_OR_ZERO = "cursorCountMustBePositiveOrZero";
    public static final String CONTEXT_ID_MUST_BE_POSITIVE = "contextIdMustBePositive";
    public static final String CURSOR_TIME_PAST_OR_PRESENT = "cursorTimeMustBePastOrPresent";

    // Pre-signed URL / File
    public static final String FILE_NAME_REQUIRED = "fileNameRequired";
    public static final String CONTENT_TYPE_REQUIRED = "contentTypeRequired";
    public static final String FILE_LIST_MUST_NOT_BE_EMPTY = "fileListMustNotBeEmpty";
    public static final String REQUEST_COUNT_EXCEEDED = "requestCountExceeded";

}
