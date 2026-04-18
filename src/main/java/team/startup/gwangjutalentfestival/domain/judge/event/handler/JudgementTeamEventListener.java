package team.startup.gwangjutalentfestival.domain.judge.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgementTeamEvent;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEmitterManager;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEventListener;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;

@Component
@RequiredArgsConstructor
public class JudgementTeamEventListener extends AbstractSseEventListener<JudgementTeamEvent> {

    private final JudgeSseEmitterManager judgeSseEmitterManager;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return judgeSseEmitterManager;
    }

    @Override
    protected String getEventName() {
        return "PERFORM_TEAM_CHANGE";
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(JudgementTeamEvent event) {
        sendToAll(event);
    }
}
