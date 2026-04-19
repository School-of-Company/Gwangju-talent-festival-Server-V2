package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.stereotype.Component;

/**
 * 좌석(Seat) 도메인의 SSE Emitter 관리자.
 * <p>좌석 구역 문자열({@link String})을 키로 사용하여 좌석 변경 SSE 연결을 관리한다.</p>
 */
@Component
public class SeatSseEmitterManager extends AbstractSseEmitterManager<String> {
}
