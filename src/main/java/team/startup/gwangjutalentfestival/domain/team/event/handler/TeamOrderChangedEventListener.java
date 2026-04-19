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

/**
 * 팀 공연 순서 변경 이벤트 리스너.
 * 트랜잭션 커밋 이후 비동기로 실행되며, 연결된 모든 심사위원 SSE 클라이언트에 순서 변경 이벤트를 전송한다.
 */
@Component
@RequiredArgsConstructor
public class TeamOrderChangedEventListener extends AbstractSseEventListener<TeamOrderChangedEvent> {

    private final JudgeSseEmitterManager judgeSseEmitterManager;

    /**
     * SSE Emitter 관리자를 반환한다.
     *
     * @return 심사위원용 SSE Emitter 관리자
     */
    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return judgeSseEmitterManager;
    }

    /**
     * SSE 이벤트 이름을 반환한다.
     *
     * @return 이벤트 이름 {@code "TEAM_ORDER_CHANGE"}
     */
    @Override
    protected String getEventName() {
        return "TEAM_ORDER_CHANGE";
    }

    /**
     * 팀 공연 순서 변경 이벤트를 수신하여 모든 SSE 클라이언트에 전송한다.
     * 트랜잭션 커밋 후 비동기로 실행된다.
     *
     * @param event 팀 공연 순서 변경 이벤트
     */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(TeamOrderChangedEvent event) {
        sendToAll(event);
    }
}
