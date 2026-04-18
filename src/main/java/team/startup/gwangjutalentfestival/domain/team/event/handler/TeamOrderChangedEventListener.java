package team.startup.gwangjutalentfestival.domain.team.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.team.event.TeamOrderChangedEvent;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEmitterManager;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEventListener;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;

@Component
@RequiredArgsConstructor
public class TeamOrderChangedEventListener extends AbstractSseEventListener<TeamOrderChangedEvent> {

    private final JudgeSseEmitterManager judgeSseEmitterManager;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return judgeSseEmitterManager;
    }

    @Override
    protected String getEventName() {
        return "TEAM_ORDER_CHANGE";
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(TeamOrderChangedEvent event) {
        sendToAll(event);
    }
}
