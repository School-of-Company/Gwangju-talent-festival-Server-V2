package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSseEmitterManager<K> {

    private static final long SSE_TIMEOUT_MILLIS = 60 * 60 * 1000L;

    protected final Map<K, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(K key) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        SseEmitter oldEmitter = emitters.put(key, emitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        emitter.onCompletion(() -> emitters.remove(key, emitter));
        emitter.onTimeout(() -> emitters.remove(key, emitter));
        emitter.onError(e -> emitters.remove(key, emitter));

        return emitter;
    }

    public Optional<SseEmitter> getEmitter(K key) {
        return Optional.ofNullable(emitters.get(key));
    }

    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
