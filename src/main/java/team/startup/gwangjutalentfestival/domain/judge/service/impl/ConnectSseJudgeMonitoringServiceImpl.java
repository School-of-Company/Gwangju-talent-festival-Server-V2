package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeMonitoringService;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeMonitoringService;
import team.startup.gwangjutalentfestival.global.sse.JudgeMonitoringSseEmitterManager;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ConnectSseJudgeMonitoringServiceImpl implements ConnectSseJudgeMonitoringService {

    private final JudgeMonitoringSseEmitterManager emitterManager;
    private final GetJudgeMonitoringService getJudgeMonitoringService;
    private final TaskScheduler taskScheduler;

    @Override
    public SseEmitter execute() {
        Long userId = UserUtil.getCurrentUserId();
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> heartbeat = new AtomicReference<>();
        SseEmitter emitter = emitterManager.addEmitter(userId, () -> {
            cancelled.set(true);
            ScheduledFuture<?> scheduled = heartbeat.get();
            if (scheduled != null) scheduled.cancel(true);
        });

        try {
            emitterManager.sendSafely(emitter, target -> target.send(
                    SseEmitter.event().name("judge-monitoring").data(getJudgeMonitoringService.execute())));
        } catch (Exception e) {
            emitterManager.completeWithErrorSafely(emitter, e);
            return emitter;
        }

        ScheduledFuture<?> scheduled = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                emitterManager.trySendSafely(emitter, target -> target.send(
                        SseEmitter.event().name("heartbeat").data("ok")));
            } catch (Exception e) {
                emitterManager.completeWithErrorSafely(emitter, e);
            }
        }, Duration.ofSeconds(15));
        heartbeat.set(scheduled);
        if (cancelled.get()) scheduled.cancel(true);
        return emitter;
    }
}
