package team.startup.gwangjutalentfestival.domain.seat.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ConnectSseSeatEventService {
    SseEmitter execute();
}
