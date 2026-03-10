package team.startup.gwangjutalentfestival.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(404, "유저를 찾을 수 없습니다."),
    DUPLICATE_PHONE_NUMBER(409, "이미 사용중인 전화번호입니다."),

    // Auth
    INVALID_PASSWORD(401, "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    REFRESH_TOKEN_NOT_FOUND(404, "Refresh Token을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),
    TOKEN_MISMATCH(401, "저장된 토큰과 일치하지 않습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // OAuth
    OAUTH_PROVIDER_NOT_SUPPORTED(400, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_PROCESSING_ERROR(500, "OAuth 처리 중 오류가 발생했습니다."),

    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
