package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JudgeSseEmitterManager {
    private static final long SSE_TIMEOUT_MILLIS = 60 * 60 * 1000;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(userId);
        });
        emitter.onError(e -> {
            emitter.completeWithError(e);
            emitters.remove(userId);
        });

        return emitter;
    }

    public Optional<SseEmitter> getEmitter(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
