package team.startup.gwangjutalentfestival.global.exception;

/**
 * API 에러 응답 DTO.
 * <p>HTTP 상태 코드와 에러 메시지를 담아 클라이언트에 반환한다.</p>
 *
 * @param status  HTTP 상태 코드
 * @param message 에러 메시지
 */
public record ErrorResponse(
        int status,
        String message
) {
}
