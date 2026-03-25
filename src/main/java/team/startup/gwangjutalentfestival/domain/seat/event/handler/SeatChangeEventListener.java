package team.startup.gwangjutalentfestival.domain.seat.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.global.sse.SeatSseEmitterManager;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SeatChangeEventListener {

    private final SeatSseEmitterManager sseEmitterManager;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(SeatChangeEvent data) {
        for (SseEmitter emitter : sseEmitterManager.getAllEmitters()) {
            try {
                emitter.send(SseEmitter.event().name("SEAT_CHANGE").data(data));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
