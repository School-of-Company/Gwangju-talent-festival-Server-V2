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

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(SeatChangeEvent event) {
        sendToAll(event);
    }
}
