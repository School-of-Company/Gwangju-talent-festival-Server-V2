package team.startup.gwangjutalentfestival.global.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeSseEmitterManagerTest {

    private JudgeSseEmitterManager manager;

    @BeforeEach
    void setUp() {
        manager = new JudgeSseEmitterManager();
    }

    @Test
    void addEmitter_호출_시_SseEmitter가_반환된다() {
        SseEmitter emitter = manager.addEmitter(1L);

        assertThat(emitter).isNotNull();
    }

    @Test
    void addEmitter_후_getEmitter로_동일한_emitter를_조회할_수_있다() {
        SseEmitter emitter = manager.addEmitter(1L);

        Optional<SseEmitter> found = manager.getEmitter(1L);

        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(emitter);
    }

    @Test
    void 등록되지_않은_userId로_getEmitter를_호출하면_empty가_반환된다() {
        Optional<SseEmitter> found = manager.getEmitter(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void getAllEmitters는_등록된_모든_emitter를_반환한다() {
        manager.addEmitter(1L);
        manager.addEmitter(2L);
        manager.addEmitter(3L);

        assertThat(manager.getAllEmitters()).hasSize(3);
    }

    @Test
    void 동일한_userId로_재연결하면_새로운_emitter로_교체된다() {
        SseEmitter first = manager.addEmitter(1L);
        SseEmitter second = manager.addEmitter(1L);

        Optional<SseEmitter> found = manager.getEmitter(1L);

        assertThat(found).isPresent();
        assertThat(found.get()).isSameAs(second);
        assertThat(found.get()).isNotSameAs(first);
    }

    @Test
    void getAllEmitters는_emitter가_없으면_빈_컬렉션을_반환한다() {
        assertThat(manager.getAllEmitters()).isEmpty();
    }
}
