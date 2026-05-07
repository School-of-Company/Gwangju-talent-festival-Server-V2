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

/**
 * {@link JudgementTeamEvent} 를 수신하여 SSE 구독자 전원에게 이벤트를 전송하는 리스너.
 * 트랜잭션 커밋 이후 비동기로 동작한다.
 */
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

    /**
     * 트랜잭션 커밋 이후 비동기로 호출되어 모든 SSE 구독자에게 이벤트를 전송한다.
     *
     * @param event 발행된 {@link JudgementTeamEvent}
     */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(JudgementTeamEvent event) {
        sendToAll(event);
    }
}
