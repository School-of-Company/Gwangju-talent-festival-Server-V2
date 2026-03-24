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

    // Verify
    EXPIRED_VERIFY_CODE(401, "인증번호가 만료되었습니다."),
    INVALID_VERIFY_CODE(400, "인증번호가 올바르지 않습니다."),

    // SMS
    SMS_SEND_FAILED(500, "SMS 전송에 실패했습니다."),
    SMS_EMPTY_RESPONSE(500, "SMS 응답이 없습니다."),
    ALREADY_VERIFY_CODE_EXISTS(429, "이미 인증번호가 발송되었습니다. 만료 후 다시 요청해주세요."),
    // Slogan
    SLOGAN_SUBMISSION_PERIOD_INVALID(400, "슬로건 접수 가능 기간이 아닙니다."),

    // Seat
    SEAT_NOT_EXISTS_IN_SECTION(400, "해당 섹션에 존재하지 않는 좌석입니다."),
    SEAT_ALREADY_RESERVED(400, "이미 예약된 좌석입니다."),
    SEAT_BANNED(400, "관리자에 의해 금지된 좌석입니다."),
    SEAT_RESERVATION_LIMIT_EXCEEDED(400, "예약 한도를 초과했습니다."),
    SEAT_NOT_FOUND(404, "예약된 좌석이 없습니다."),
    SEAT_BAN_NOT_FOUND(404, "금지되지 않은 상태의 자리입니다."),

    // Google Sheets
    GOOGLE_SHEETS(500, "Google Sheets 연동 중 오류가 발생했습니다."),
    GOOGLE_SHEETS_API_ERROR(502, "Google Sheets API 오류가 발생했습니다."),
    GOOGLE_SHEETS_IO_ERROR(503, "Google Sheets 서버와 연결할 수 없습니다."),
    GOOGLE_SHEETS_INIT_ERROR(500, "Google Sheets 초기화 중 오류가 발생했습니다."),

    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
