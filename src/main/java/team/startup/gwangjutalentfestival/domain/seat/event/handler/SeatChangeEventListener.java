package team.startup.gwangjutalentfestival.domain.seat.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEmitterManager;
import team.startup.gwangjutalentfestival.global.sse.AbstractSseEventListener;
import team.startup.gwangjutalentfestival.global.sse.SeatSseEmitterManager;

/**
 * 좌석 변경 이벤트를 수신하여 모든 SSE 구독 클라이언트에 알림을 전송하는 리스너.
 * 트랜잭션 커밋 이후 비동기로 실행되며, "SEAT_CHANGE" 이름의 이벤트를 발행한다.
 */
@Component
@RequiredArgsConstructor
public class SeatChangeEventListener extends AbstractSseEventListener<SeatChangeEvent> {

    private final SeatSseEmitterManager sseEmitterManager;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() {
        return sseEmitterManager;
    }

    @Override
    protected String getEventName() {
        return "SEAT_CHANGE";
    }

    /**
     * 트랜잭션 커밋 후 비동기로 모든 SSE 클라이언트에 좌석 변경 이벤트를 전송한다.
     *
     * @param event 전송할 좌석 변경 이벤트
     */
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(SeatChangeEvent event) {
        sendToAll(event);
    }
}
