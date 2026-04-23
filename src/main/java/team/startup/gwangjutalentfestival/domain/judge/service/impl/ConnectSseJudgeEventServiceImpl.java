package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.judge.service.ConnectSseJudgeEventService;
import team.startup.gwangjutalentfestival.global.sse.JudgeSseEmitterManager;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConnectSseJudgeEventService} 구현체.
 * SSE 연결 수립 후 최초 연결 이벤트를 전송하고, 주기적으로 heartbeat를 전송한다.
 */
@Service
@RequiredArgsConstructor
public class ConnectSseJudgeEventServiceImpl implements ConnectSseJudgeEventService {
    private final JudgeSseEmitterManager judgeSseEmitterManager;
    private final TaskScheduler taskScheduler;

    private static final String CONNECTED_EVENT = "connected";
    private static final String HEARTBEAT_EVENT = "heartbeat";
    private static final String CONNECTED_DATA = "ok";

    /**
     * 현재 로그인한 심사위원에 대한 SSE 연결을 생성하고,
     * 초기 연결 이벤트 전송 및 heartbeat 스케줄러를 설정한 후 반환한다.
     *
     * @return 생성된 {@link SseEmitter}
     */
    @Override
    public SseEmitter execute() {
        Long userId = UserUtil.getCurrentUserId();

        AtomicReference<ScheduledFuture<?>> beatHolder = new AtomicReference<>();
        SseEmitter emitter = judgeSseEmitterManager.addEmitter(userId,
                () -> { ScheduledFuture<?> b = beatHolder.get(); if (b != null) b.cancel(true); });

        try {
            emitter.send(SseEmitter.event()
                    .name(CONNECTED_EVENT)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .data(CONNECTED_DATA));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        ScheduledFuture<?> beat = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(HEARTBEAT_EVENT)
                        .id(String.valueOf(System.currentTimeMillis()))
                        .data(CONNECTED_DATA));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, Duration.ofSeconds(15));
        beatHolder.set(beat);

        return emitter;
    }
}
