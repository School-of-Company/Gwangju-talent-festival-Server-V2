package team.startup.gwangjutalentfestival.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.util.concurrent.Executor;

/**
 * SSE 이벤트를 전체 구독자에게 전송하는 추상 리스너.
 * <p>구체 클래스는 {@link #getEmitterManager()}와 {@link #getEventName()}을 구현하여
 * 도메인별 Emitter 관리자와 이벤트 이름을 제공한다.
 * {@link #sendToAll(Object)}를 호출하면 {@link AbstractSseEmitterManager#forEachEmitterSafe}를 통해
 * 락을 보유한 상태로 모든 SSE 클라이언트에 이벤트가 전송된다.</p>
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
     * 전송할 SSE 이벤트 이름을 반환한다.
     *
     * @return 이벤트 이름
     */
    protected abstract String getEventName();

    /**
     * 현재 연결된 모든 SSE 클라이언트에 이벤트를 전송한다.
     *
     * @param event 전송할 이벤트 데이터
     */
    protected void sendToAll(E event) {
        getEmitterManager().forEachEmitterSafe(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(getEventName()).data(event));
            } catch (Exception e) {
                log.error("SSE 전송 실패", e);
                getEmitterManager().completeWithErrorSafely(emitter, e);
            }
        });
    }

    protected void sendBoundedTextToAll(String event, Executor executor) {
        getEmitterManager().forEachEmitterBounded(emitter ->
                emitter.send(SseEmitter.event().name(getEventName()).data(event, MediaType.TEXT_PLAIN)), executor);
    }
}
