package team.startup.gwangjutalentfestival.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
public abstract class AbstractSseEventListener<E> {

    protected abstract AbstractSseEmitterManager<?> getEmitterManager();

    protected abstract String getEventName();

    protected void sendToAll(E event) {
        for (SseEmitter emitter : getEmitterManager().getAllEmitters()) {
            try {
                emitter.send(SseEmitter.event().name(getEventName()).data(event));
            } catch (IOException e) {
                log.error("SSE 전송 실패", e);
                emitter.completeWithError(e);
            }
        }
    }
}
