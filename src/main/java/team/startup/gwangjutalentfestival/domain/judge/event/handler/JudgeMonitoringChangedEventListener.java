package team.startup.gwangjutalentfestival.domain.judge.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgeMonitoringChangedEvent;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringResponse;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeMonitoringService;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEmitterManager;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEventListener;
import team.startup.gwangjutalentfestival.global.sse.JudgeMonitoringSseEmitterManager;

@Component
@RequiredArgsConstructor
public class JudgeMonitoringChangedEventListener extends AbstractSseEventListener<JudgeMonitoringResponse> {

    private final JudgeMonitoringSseEmitterManager emitterManager;
    private final GetJudgeMonitoringService getJudgeMonitoringService;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return emitterManager;
    }

    @Override
    protected String getEventName() {
        return "judge-monitoring";
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(JudgeMonitoringChangedEvent event) {
        sendToAll(getJudgeMonitoringService.execute());
    }
}
