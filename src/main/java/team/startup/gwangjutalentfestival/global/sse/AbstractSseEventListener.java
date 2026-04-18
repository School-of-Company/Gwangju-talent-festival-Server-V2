package team.startup.gwangjutalentfestival.global.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public abstract class AbstractSseEventListener<E> {

    protected abstract AbstractSseEmitterManager<?> getEmitterManager();

    protected abstract String getEventName();

    protected void sendToAll(E event) {
        for (SseEmitter emitter : getEmitterManager().getAllEmitters()) {
            try {
                emitter.send(SseEmitter.event().name(getEventName()).data(event));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
