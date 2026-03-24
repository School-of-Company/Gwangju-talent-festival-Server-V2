package team.startup.gwangjutalentfestival.domain.team.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.team.event.TeamOrderChangedEvent;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamOrderChangedEventListener {
    private final JudgeSseEmitterManager judgeSseEmitterManager;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(TeamOrderChangedEvent event) {
        for (SseEmitter emitter : judgeSseEmitterManager.getAllEmitters()) {
            try {
                emitter.send(SseEmitter.event().name("TEAM_ORDER_CHANGE").data(event));
            } catch (IOException e) {
                log.error("SSE 전송 실패", e);
                emitter.completeWithError(e);
            }
        }
    }
}
