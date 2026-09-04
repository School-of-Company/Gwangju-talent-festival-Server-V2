package team.startup.gwangjutalentfestival.global.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class JudgeSseEmitterManagerTest {

    private JudgeSseEmitterManager manager;

    @BeforeEach
    void setUp() {
        manager = new JudgeSseEmitterManager();
    }

    @Test
    void addEmitter_호출_시_SseEmitter가_반환된다() {
        SseEmitter emitter = manager.addEmitter(1L, null);

        assertThat(emitter).isNotNull();
    }

    @Test
    void 동일한_userId로_여러_연결을_등록하면_모두_유지된다() {
        SseEmitter first = manager.addEmitter(1L, null);
        SseEmitter second = manager.addEmitter(1L, null);

        assertThat(manager.getAllEmitters()).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void getAllEmitters는_등록된_모든_emitter를_반환한다() {
        manager.addEmitter(1L, null);
        manager.addEmitter(2L, null);
        manager.addEmitter(3L, null);

        assertThat(manager.getAllEmitters()).hasSize(3);
    }

    @Test
    void 동일한_userId의_모든_emitter에_이벤트를_전송한다() {
        manager.addEmitter(1L, null);
        manager.addEmitter(1L, null);
        AtomicInteger sent = new AtomicInteger();

        manager.forEachEmitterSafe(ignored -> sent.incrementAndGet());

        assertThat(sent).hasValue(2);
    }

    @Test
    void 동일한_userId로_동시에_연결해도_모든_emitter가_등록된다() {
        IntStream.range(0, 100).parallel()
                .forEach(ignored -> manager.addEmitter(1L, null));

        assertThat(manager.getAllEmitters()).hasSize(100);
    }

    @Test
    void getAllEmitters는_emitter가_없으면_빈_컬렉션을_반환한다() {
        assertThat(manager.getAllEmitters()).isEmpty();
    }

    @Test
    void 전송_중에는_작업_여덟개만_대기시키고_초과_작업은_버린다() throws Exception {
        SseEmitter emitter = manager.addEmitter(1L, null);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);
        var sent = new CopyOnWriteArrayList<Integer>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            manager.forEachEmitterBounded(ignored -> {
                sent.add(1);
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executor);
            assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();

            IntStream.rangeClosed(2, 9).forEach(value ->
                    manager.forEachEmitterBounded(ignored -> {
                        sent.add(value);
                        if (value == 9) completed.countDown();
                    }, executor));
            manager.forEachEmitterBounded(ignored -> sent.add(10), executor);
            assertThat(manager.trySendSafely(emitter, ignored -> sent.add(99))).isFalse();

            release.countDown();
            assertThat(completed.await(1, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(sent).containsExactlyElementsOf(IntStream.rangeClosed(1, 9).boxed().toList());
    }

    @Test
    void 느린_emitter가_다른_emitter의_전송을_막지_않는다() throws Exception {
        SseEmitter slow = manager.addEmitter(1L, null);
        manager.addEmitter(2L, null);
        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch releaseSlow = new CountDownLatch(1);
        CountDownLatch fastCompleted = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            manager.forEachEmitterBounded(emitter -> {
                if (emitter == slow) {
                    slowStarted.countDown();
                    try {
                        releaseSlow.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    fastCompleted.countDown();
                }
            }, executor);

            assertThat(slowStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(fastCompleted.await(1, TimeUnit.SECONDS)).isTrue();
            releaseSlow.countDown();
        }
    }

    @Test
    void 이미_종료된_emitter의_중복_완료는_무시한다() {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IllegalStateException("already completed"))
                .when(emitter).completeWithError(any(Throwable.class));

        manager.completeWithErrorSafely(emitter, new IOException("send failed"));
    }
}
