package team.startup.gwangjutalentfestival.domain.judge.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 심사 이벤트 SSE 연결을 처리하는 서비스 인터페이스.
 */
public interface ConnectSseJudgeEventService {

    /**
     * 현재 로그인한 심사위원에 대한 SSE 연결을 생성하고 반환한다.
     *
     * @return 생성된 {@link SseEmitter}
     */
    SseEmitter execute();
}
