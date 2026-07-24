package team.startup.gwangjutalentfestival.domain.judge.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ConnectSseJudgeMonitoringService {
    SseEmitter execute();
}
