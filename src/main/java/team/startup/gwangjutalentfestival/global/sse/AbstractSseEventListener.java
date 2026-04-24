package team.startup.gwangjutalentfestival.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * SSE 이벤트를 전체 구독자에게 전송하는 추상 리스너.
 * <p>구체 클래스는 {@link #getEmitterManager()}와 {@link #getEventName()}을 구현하여
 * 도메인별 Emitter 관리자와 이벤트 이름을 제공한다.
 * {@link #sendToAll(Object)}를 호출하면 연결된 모든 SSE 클라이언트에 이벤트가 전송된다.</p>
 *
 * @param <E> 전송할 이벤트 데이터 타입
 */
@Slf4j
public abstract class AbstractSseEventListener<E> {

    /**
     * 이벤트를 전송할 {@link AbstractSseEmitterManager}를 반환한다.
     *
     * @return 도메인별 Emitter 관리자
     */
    protected abstract AbstractSseEmitterManager<?> getEmitterManager();

    /**
     * SSE 이벤트 이름을 반환한다.
     *
     * @return 이벤트 이름 문자열
     */
    protected abstract String getEventName();

    /**
     * 연결된 모든 SSE 클라이언트에 이벤트를 전송한다.
     * <p>각 Emitter에 동기화(synchronized)하여 스레드 안전성을 보장하며,
     * 전송 실패 시 해당 Emitter를 오류 완료 처리한다.</p>
     *
     * @param event 전송할 이벤트 데이터
     */
    protected void sendToAll(E event) {
        getEmitterManager().getAllEmitters().forEach(emitter -> {
            synchronized (emitter) {
                try {
                    emitter.send(SseEmitter.event().name(getEventName()).data(event));
                } catch (Exception e) {
                    log.error("SSE 전송 실패", e);
                    emitter.completeWithError(e);
                }
            }
        });
    }
}
