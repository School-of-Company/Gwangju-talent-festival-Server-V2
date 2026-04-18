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
        emitters.put(key, emitter);

        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(key);
        });
        emitter.onError(e -> {
            emitter.completeWithError(e);
            emitters.remove(key);
        });

        return emitter;
    }

    public Optional<SseEmitter> getEmitter(K key) {
        return Optional.ofNullable(emitters.get(key));
    }

    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
