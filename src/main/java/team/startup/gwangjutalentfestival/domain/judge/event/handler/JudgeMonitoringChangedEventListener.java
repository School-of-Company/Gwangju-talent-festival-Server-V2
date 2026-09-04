package team.startup.gwangjutalentfestival.domain.judge.event.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgeMonitoringChangedEvent;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringDeltaResponse;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeMonitoringService;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEmitterManager;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEventListener;
import team.startup.gwangjutalentfestival.global.sse.JudgeMonitoringSseEmitterManager;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
public class JudgeMonitoringChangedEventListener extends AbstractSseEventListener<JudgeMonitoringDeltaResponse> {

    private final JudgeMonitoringSseEmitterManager emitterManager;
    private final GetJudgeMonitoringService getJudgeMonitoringService;
    private final TaskScheduler taskScheduler;
    private final Executor asyncExecutor;
    private final ObjectMapper objectMapper;

    private static final Duration COALESCING_WINDOW = Duration.ofMillis(200);
    private final Map<CommentKey, JudgeMonitoringDeltaResponse.CommentDelta> comments = new LinkedHashMap<>();
    private boolean scoreDirty;
    private boolean scheduled;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return emitterManager;
    }

    @Override
    protected String getEventName() {
        return "judge-monitoring-delta";
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(JudgeMonitoringChangedEvent event) {
        if (emitterManager.getAllEmitters().isEmpty()) return;
        synchronized (this) {
            if (event.type() == JudgeMonitoringChangedEvent.Type.SCORE) {
                scoreDirty = true;
            } else {
                comments.put(new CommentKey(event.teamId(), event.judgeId()),
                        new JudgeMonitoringDeltaResponse.CommentDelta(
                                event.teamId(), event.judgeId()));
            }
            scheduleRefresh();
        }
    }

    private void scheduleRefresh() {
        if (scheduled) return;
        scheduled = true;
        try {
            taskScheduler.schedule(this::refresh, Instant.now().plus(COALESCING_WINDOW));
        } catch (RuntimeException e) {
            scheduled = false;
            throw e;
        }
    }

    private void refresh() {
        boolean includeScores;
        List<JudgeMonitoringDeltaResponse.CommentDelta> commentDeltas;
        synchronized (this) {
            includeScores = scoreDirty;
            commentDeltas = List.copyOf(comments.values());
            scoreDirty = false;
            comments.clear();
        }
        try {
            if (!emitterManager.getAllEmitters().isEmpty()) {
                JudgeMonitoringDeltaResponse.ScoreSnapshot scores =
                        includeScores ? getJudgeMonitoringService.executeScores() : null;
                JudgeMonitoringDeltaResponse delta = new JudgeMonitoringDeltaResponse(
                        getJudgeMonitoringService.nextVersion(), scores, commentDeltas);
                sendBoundedTextToAll(writeValueAsString(delta), asyncExecutor);
            }
        } finally {
            synchronized (this) {
                scheduled = false;
                if (scoreDirty || !comments.isEmpty()) scheduleRefresh();
            }
        }
    }

    private String writeValueAsString(JudgeMonitoringDeltaResponse delta) {
        try {
            return objectMapper.writeValueAsString(delta);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("judge monitoring delta를 직렬화할 수 없습니다.", e);
        }
    }

    private record CommentKey(Long teamId, Long judgeId) {
    }
}
