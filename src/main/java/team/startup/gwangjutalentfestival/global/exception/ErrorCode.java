package team.startup.gwangjutalentfestival.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 애플리케이션 전반에서 사용하는 에러 코드 열거형.
 * <p>각 항목은 HTTP 상태 코드와 사용자 메시지를 포함한다.</p>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(404, "유저를 찾을 수 없습니다."),

    // Team
    TEAM_NOT_FOUND(404, "팀을 찾을 수 없습니다."),
    TEAM_ALREADY_FINISHED(409, "이미 종료된 공연입니다."),
    TEAM_ALREADY_ONGOING(409, "이미 공연이 진행 중인 팀입니다."),
    DUPLICATE_PHONE_NUMBER(409, "이미 사용중인 전화번호입니다."),

    // Auth
    INVALID_PASSWORD(401, "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    INVALID_ACCESS_TOKEN(401, "유효하지 않은 Access Token입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    REFRESH_TOKEN_NOT_FOUND(404, "Refresh Token을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // Performer
    INVALID_PERFORMER_VERIFICATION(400, "출연진 인증 정보가 올바르지 않습니다."),
    PERFORMER_VERIFICATION_ALREADY_CLAIMED(409, "이미 사용된 출연진 인증 정보입니다."),
    ALREADY_PERFORMER(409, "이미 출연진 인증을 완료한 계정입니다."),

    // Verify
    EXPIRED_VERIFY_CODE(401, "인증번호가 만료되었습니다."),
    INVALID_VERIFY_CODE(400, "인증번호가 올바르지 않습니다."),

    // SMS
    SMS_SEND_FAILED(500, "SMS 전송에 실패했습니다."),
    SMS_EMPTY_RESPONSE(500, "SMS 응답이 없습니다."),
    ALREADY_VERIFY_CODE_EXISTS(429, "이미 인증번호가 발송되었습니다. 만료 후 다시 요청해주세요."),
    // Slogan
    SLOGAN_SUBMISSION_PERIOD_INVALID(400, "슬로건 접수 가능 기간이 아닙니다."),
    SLOGAN_REQUIRED_FIELD_MISSING(400, "필수 항목이 누락되었습니다."),
    INVALID_AGE(400, "나이는 7세 이상 18세 이하(출생 연도 기준)이어야 합니다."),

    // Seat
    SEAT_NOT_EXISTS_IN_SECTION(400, "해당 섹션에 존재하지 않는 좌석입니다."),
    SEAT_ALREADY_RESERVED(400, "이미 예약된 좌석입니다."),
    SEAT_BANNED(400, "예약할 수 없는 좌석입니다."),
    SEAT_ALREADY_BANNED(400, "이미 금지된 좌석입니다."),
    INVALID_SEAT_SECTION(400, "유효하지 않은 좌석 구역입니다."),
    SEAT_RESERVATION_LIMIT_EXCEEDED(400, "예약 한도를 초과했습니다."),
    SEAT_NOT_FOUND(404, "예약된 좌석이 없습니다."),
    SEAT_BAN_NOT_FOUND(404, "금지되지 않은 상태의 자리입니다."),

    // Judge
    JUDGE_COMMENT_TOO_LARGE(400, "필기 데이터가 너무 큽니다."),

    // Monitoring
    ANOMALY_EVENT_NOT_FOUND(404, "이상 탐지 이벤트를 찾을 수 없습니다."),
    FEEDBACK_ALREADY_EXISTS(409, "이미 피드백이 등록된 이벤트입니다."),
    ANOMALY_EVENT_NOT_OPEN(400, "이미 처리된 이상 탐지 이벤트입니다."),
    EXPORT_INVALID_PERIOD(400, "end는 start보다 이후여야 합니다."),
    EXPORT_PERIOD_EXCEEDED(400, "export 기간은 최대 7일입니다."),
    EXPORT_INVALID_FILTER(400, "허용되지 않는 domain 또는 metricName입니다."),
    EXPORT_FAILED(500, "데이터셋 export 중 오류가 발생했습니다."),

    // S3
    INVALID_VIDEO_FILE(400, "유효하지 않은 영상 파일입니다."),
    MULTIPART_UPLOAD_FAILED(500, "영상 업로드 처리에 실패했습니다."),

    // Apply
    APPLY_NOT_FOUND(404, "신청 내역을 찾을 수 없습니다."),

    // Google Sheets
    GOOGLE_SHEETS(500, "Google Sheets 연동 중 오류가 발생했습니다."),
    GOOGLE_SHEETS_API_ERROR(502, "Google Sheets API 오류가 발생했습니다."),
    GOOGLE_SHEETS_IO_ERROR(503, "Google Sheets 서버와 연결할 수 없습니다."),
    GOOGLE_SHEETS_INIT_ERROR(500, "Google Sheets 초기화 중 오류가 발생했습니다."),

    // RateLimit
    TOO_MANY_REQUESTS(429, "요청이 너무 잦습니다. 잠시 후 다시 시도해주세요."),

    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
