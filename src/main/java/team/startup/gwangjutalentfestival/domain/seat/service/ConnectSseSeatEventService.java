package team.startup.gwangjutalentfestival.domain.seat.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 좌석 실시간 변경 이벤트를 수신하기 위한 SSE 연결을 생성하는 서비스 인터페이스.
 */
public interface ConnectSseSeatEventService {

    /**
     * SSE 연결을 생성하고 이미터를 반환한다.
     *
     * @return 생성된 SSE 이미터
     */
    SseEmitter execute();
}
