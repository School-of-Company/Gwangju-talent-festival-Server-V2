package team.startup.gwangjutalentfestival.domain.judge.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgeMonitoringChangedEvent;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringDeltaResponse;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeMonitoringService;
import team.startup.gwangjutalentfestival.global.sse.JudgeMonitoringSseEmitterManager;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudgeMonitoringChangedEventListenerTest {

    @Mock private JudgeMonitoringSseEmitterManager emitterManager;
    @Mock private GetJudgeMonitoringService getJudgeMonitoringService;
    @Mock private TaskScheduler taskScheduler;

    private JudgeMonitoringChangedEventListener listener;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;
        listener = new JudgeMonitoringChangedEventListener(
                emitterManager, getJudgeMonitoringService, taskScheduler, directExecutor, new ObjectMapper());
    }

    @Test
    void 짧은_구간의_같은_필기_변경은_최신_delta_하나로_합친다() {
        given(emitterManager.getAllEmitters()).willReturn(List.of(mock(SseEmitter.class)));
        given(getJudgeMonitoringService.nextVersion()).willReturn(1L);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        listener.execute(JudgeMonitoringChangedEvent.commentChanged(10L, 1L));
        listener.execute(JudgeMonitoringChangedEvent.commentChanged(10L, 1L));

        verify(taskScheduler).schedule(task.capture(), any(Instant.class));
        task.getValue().run();
        verify(getJudgeMonitoringService, never()).executeScores();
        verify(emitterManager).forEachEmitterBounded(any(), any());
    }

    @Test
    void 스냅샷_계산_중_발생한_이벤트는_후속_갱신을_예약한다() {
        given(emitterManager.getAllEmitters()).willReturn(List.of(mock(SseEmitter.class)));
        given(getJudgeMonitoringService.executeScores()).willAnswer(ignored -> {
            listener.execute(JudgeMonitoringChangedEvent.scoreChanged());
            return scores();
        });
        ArgumentCaptor<Runnable> tasks = ArgumentCaptor.forClass(Runnable.class);

        listener.execute(JudgeMonitoringChangedEvent.scoreChanged());
        verify(taskScheduler).schedule(tasks.capture(), any(Instant.class));
        tasks.getValue().run();

        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void 구독자가_없으면_갱신을_예약하지_않는다() {
        given(emitterManager.getAllEmitters()).willReturn(List.of());

        listener.execute(JudgeMonitoringChangedEvent.scoreChanged());

        verifyNoInteractions(taskScheduler, getJudgeMonitoringService);
    }

    private JudgeMonitoringDeltaResponse.ScoreSnapshot scores() {
        return new JudgeMonitoringDeltaResponse.ScoreSnapshot(List.of(), List.of());
    }
}
