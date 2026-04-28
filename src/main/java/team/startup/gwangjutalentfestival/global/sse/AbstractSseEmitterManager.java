package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class AbstractSseEmitterManager<K> {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final Map<K, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ReentrantLock> locks = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(K key, Runnable onCleanup) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        locks.put(emitter, new ReentrantLock());

        Runnable cleanup = () -> {
            emitters.remove(key, emitter);
            locks.remove(emitter);
            if (onCleanup != null) onCleanup.run();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(e -> cleanup.run());

        SseEmitter oldEmitter = emitters.put(key, emitter);
        if (oldEmitter != null) {
            locks.remove(oldEmitter);
            oldEmitter.complete();
        }

        return emitter;
    }

    public void forEachEmitterSafe(Consumer<SseEmitter> action) {
        emitters.values().forEach(emitter -> {
            ReentrantLock lock = locks.get(emitter);
            if (lock == null) return;
            lock.lock();
            try {
                action.accept(emitter);
            } finally {
                lock.unlock();
            }
        });
    }

    public Optional<SseEmitter> getEmitter(K key) {
        return Optional.ofNullable(emitters.get(key));
    }

    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
