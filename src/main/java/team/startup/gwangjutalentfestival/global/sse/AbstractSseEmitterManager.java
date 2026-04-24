package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE Emitter를 관리하는 추상 기반 클래스.
 * <p>키 타입({@code K}) 별로 {@link SseEmitter}를 {@link java.util.concurrent.ConcurrentHashMap}으로 저장하며,
 * 연결 완료·타임아웃·에러 발생 시 자동으로 제거한다.
 * 동일 키로 재연결 시 기존 Emitter를 완료 처리하여 리소스 누수를 방지한다.</p>
 *
 * @param <K> Emitter를 식별하는 키 타입
 */
public abstract class AbstractSseEmitterManager<K> {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final Map<K, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 새 {@link SseEmitter}를 생성하고 등록한다.
     * <p>동일 키에 기존 Emitter가 있으면 완료 처리 후 교체한다.</p>
     *
     * @param key Emitter를 식별하는 키
     * @return 생성된 {@link SseEmitter}
     */
    public SseEmitter addEmitter(K key, Runnable onCleanup) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        Runnable cleanup = () -> {
            emitters.remove(key, emitter);
            if (onCleanup != null) onCleanup.run();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> { emitter.complete(); });
        emitter.onError(e -> cleanup.run());

        SseEmitter oldEmitter = emitters.put(key, emitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        return emitter;
    }

    /**
     * 키에 해당하는 {@link SseEmitter}를 조회한다.
     *
     * @param key 조회할 키
     * @return {@link SseEmitter}를 감싼 {@link Optional}, 없으면 empty
     */
    public Optional<SseEmitter> getEmitter(K key) {
        return Optional.ofNullable(emitters.get(key));
    }

    /**
     * 현재 등록된 모든 {@link SseEmitter}를 반환한다.
     *
     * @return 등록된 Emitter 컬렉션
     */
    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
