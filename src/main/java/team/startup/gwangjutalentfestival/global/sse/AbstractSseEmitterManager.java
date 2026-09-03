package team.startup.gwangjutalentfestival.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * SSE Emitter를 관리하는 추상 기반 클래스.
 * <p>키 타입({@code K}) 별로 여러 {@link SseEmitter}를 {@link ConcurrentHashMap}으로 저장하며,
 * 연결 완료·타임아웃·에러 발생 시 자동으로 제거한다.
 * 가상 스레드 환경에서 carrier thread pinning 없이 안전한 전송을 위해
 * {@link ReentrantLock}을 Emitter별로 관리한다.</p>
 *
 * @param <K> Emitter를 식별하는 키 타입
 */
@Slf4j
public abstract class AbstractSseEmitterManager<K> {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;
    private static final int MAX_PENDING_ACTIONS = 8;

    private final Map<K, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Map<SseEmitter, BlockingQueue<EmitterAction>> pendingActions = new ConcurrentHashMap<>();
    private final Set<SseEmitter> drainingEmitters = ConcurrentHashMap.newKeySet();
    private final Set<SseEmitter> overflowedEmitters = ConcurrentHashMap.newKeySet();

    @FunctionalInterface
    public interface EmitterAction {
        void accept(SseEmitter emitter) throws IOException;
    }

    /**
     * 새 {@link SseEmitter}를 생성하고 등록한다.
     *
     * @param key       Emitter를 식별하는 키
     * @param onCleanup 연결 종료 시 실행할 콜백
     * @return 생성된 {@link SseEmitter}
     */
    public SseEmitter addEmitter(K key, Runnable onCleanup) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        locks.put(emitter, new ReentrantLock());

        Runnable cleanup = () -> {
            emitters.computeIfPresent(key, (ignored, connections) -> {
                connections.remove(emitter);
                return connections.isEmpty() ? null : connections;
            });
            locks.remove(emitter);
            pendingActions.remove(emitter);
            drainingEmitters.remove(emitter);
            overflowedEmitters.remove(emitter);
            if (onCleanup != null) onCleanup.run();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(e -> cleanup.run());

        emitters.compute(key, (ignored, connections) -> {
            Set<SseEmitter> current = connections == null ? ConcurrentHashMap.newKeySet() : connections;
            current.add(emitter);
            return current;
        });

        return emitter;
    }

    /**
     * 등록된 모든 Emitter에 대해 락을 획득한 상태로 안전하게 실행한다.
     *
     * @param action 각 Emitter에 수행할 작업
     */
    public void forEachEmitterSafe(Consumer<SseEmitter> action) {
        emitters.values().stream().flatMap(Collection::stream).forEach(emitter -> {
            try {
                sendSafely(emitter, action::accept);
            } catch (IOException ignored) {
                // Consumer 기반 기존 호출은 checked exception을 발생시키지 않는다.
            }
        });
    }

    public void sendSafely(SseEmitter emitter, EmitterAction action) throws IOException {
        ReentrantLock lock = locks.get(emitter);
        if (lock == null) return;
        lock.lock();
        try {
            action.accept(emitter);
        } finally {
            lock.unlock();
        }
    }

    public boolean trySendSafely(SseEmitter emitter, EmitterAction action) throws IOException {
        ReentrantLock lock = locks.get(emitter);
        if (lock == null || !lock.tryLock()) return false;
        try {
            action.accept(emitter);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void completeWithErrorSafely(SseEmitter emitter, Throwable error) {
        try {
            emitter.completeWithError(error);
        } catch (IllegalStateException ignored) {
            // 이미 종료된 emitter는 추가 완료가 필요 없다.
        }
    }

    /** Emitter별 bounded queue가 가득 차면 재연결시킨다. */
    public void forEachEmitterBounded(EmitterAction action, Executor executor) {
        getAllEmitters().forEach(emitter -> {
            BlockingQueue<EmitterAction> pending = pendingActions.computeIfAbsent(
                    emitter, ignored -> new ArrayBlockingQueue<>(MAX_PENDING_ACTIONS));
            if (!pending.offer(action)) {
                if (overflowedEmitters.add(emitter)) {
                    executor.execute(() -> completeWithErrorSafely(
                            emitter, new IOException("SSE client is too slow")));
                }
                return;
            }
            startDraining(emitter, executor);
        });
    }

    private void startDraining(SseEmitter emitter, Executor executor) {
        if (!drainingEmitters.add(emitter)) return;
        try {
            executor.execute(() -> drain(emitter, executor));
        } catch (RuntimeException e) {
            drainingEmitters.remove(emitter);
            throw e;
        }
    }

    private void drain(SseEmitter emitter, Executor executor) {
        BlockingQueue<EmitterAction> pending = pendingActions.get(emitter);
        try {
            EmitterAction action;
            while (pending != null && (action = pending.poll()) != null) {
                try {
                    sendSafely(emitter, action);
                } catch (Exception e) {
                    log.debug("SSE 연결 종료: {}", e.toString());
                    completeWithErrorSafely(emitter, e);
                    return;
                }
            }
        } finally {
            drainingEmitters.remove(emitter);
            pending = pendingActions.get(emitter);
            if (pending != null && !pending.isEmpty()) startDraining(emitter, executor);
        }
    }

    /**
     * 현재 등록된 모든 {@link SseEmitter}를 반환한다.
     *
     * @return 등록된 Emitter 컬렉션
     */
    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values().stream()
                .flatMap(Collection::stream)
                .filter(emitter -> !overflowedEmitters.contains(emitter))
                .toList();
    }
}
