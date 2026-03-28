package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeEventService;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ConnectSseJudgeEventServiceImpl implements ConnectSseJudgeEventService {
    private final JudgeSseEmitterManager judgeSseEmitterManager;
    private final UserUtil userUtil;

    private final static String CONNECTED_EVENT = "connected";
    private final static String HEARTBEAT_EVENT = "heartbeat";
    private final static String CONNECTED_DATA = "ok";
    private final static long HEARTBEAT_INTERVAL_SECONDS = 15;

    @Override
    public SseEmitter execute() {
        Long userId = userUtil.getCurrentUser().getId();
        SseEmitter emitter = judgeSseEmitterManager.addEmitter(userId);
        try {
            emitter.send(SseEmitter.event()
                    .name(CONNECTED_EVENT)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .data(CONNECTED_DATA));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicReference<ScheduledFuture<?>> beatRef = new AtomicReference<>();

        ScheduledFuture<?> beat = scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(HEARTBEAT_EVENT)
                        .id(String.valueOf(System.currentTimeMillis()))
                        .data(CONNECTED_DATA));
            } catch (IOException e) {
                ScheduledFuture<?> b = beatRef.get();
                if (b != null) b.cancel(true);
                scheduler.shutdown();
                emitter.completeWithError(e);
            }
        }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

        beatRef.set(beat);

        emitter.onCompletion(() -> { beat.cancel(true); scheduler.shutdown(); });
        emitter.onTimeout(() -> { beat.cancel(true); scheduler.shutdown(); });
        emitter.onError(e -> { beat.cancel(true); scheduler.shutdown(); });
        return emitter;
    }
}
