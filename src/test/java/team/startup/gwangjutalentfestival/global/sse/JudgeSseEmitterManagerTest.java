package team.startup.gwangjutalentfestival.global.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

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
}
