package neltia.bloguide.api.share;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseCodeEnum {
    DATA_EXISTS(200),
    FILE_EXISTS(200),
    OK(200),

    NEW_FILE_UPLOAD_SUCCESS(201),
    FILE_NEW(201),
    SUCCESS_NEW(201),

    INVALID_PARAMETER(400),

    UNAUTHORIZED(401),
    ACCOUNT_NOT_FOUND(403),
    API_KEY_NOT_FOUND(403),
    NO_AUTHORITY_TO_USE(403),

    NOT_FOUND(404),
    NO_DATA(404),
    INVALID_METHOD(405),

    DAILY_QUOTA_LIMIT_EXCEED(429),
    OVER_THE_DAILY_REQUEST_LIMIT(429),

    INTERNAL_SERVER_ERROR(500);

    private final Integer code;
}
