package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.stereotype.Component;

/**
 * 심사(Judge) 도메인의 SSE Emitter 관리자.
 * <p>사용자 ID({@link Long})를 키로 사용하여 심사 관련 SSE 연결을 관리한다.</p>
 */
@Component
public class JudgeSseEmitterManager extends AbstractSseEmitterManager<Long> {
}
