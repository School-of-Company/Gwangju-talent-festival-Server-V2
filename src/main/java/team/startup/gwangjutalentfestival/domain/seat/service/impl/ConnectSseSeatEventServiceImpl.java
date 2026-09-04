package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.startup.gwangjutalentfestival.domain.seat.service.ConnectSseSeatEventService;
import team.startup.gwangjutalentfestival.global.sse.SeatSseEmitterManager;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConnectSseSeatEventService}의 구현체.
 * SSE 이미터를 생성하고 초기 연결 이벤트를 전송하며, 15초 간격으로 하트비트를 전송한다.
 */
@Service
@RequiredArgsConstructor
public class ConnectSseSeatEventServiceImpl implements ConnectSseSeatEventService {

    private final SeatSseEmitterManager sseEmitterManager;
    private final TaskScheduler taskScheduler;

    private static final String CONNECTED_EVENT_NAME = "connected";
    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";

    /**
     * SSE 연결을 생성하고 초기 연결 이벤트를 전송한 뒤 이미터를 반환한다.
     * 연결 완료·타임아웃·에러 시 하트비트 스케줄러를 취소한다.
     *
     * @return 생성된 SSE 이미터
     */
    @Override
    public SseEmitter execute() {
        Long userId = UserUtil.getCurrentUserId();

        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> beatHolder = new AtomicReference<>();
        SseEmitter emitter = sseEmitterManager.addEmitter(userId, () -> {
            cancelled.set(true);
            ScheduledFuture<?> b = beatHolder.get();
            if (b != null) b.cancel(true);
        });

        try {
            sseEmitterManager.sendSafely(emitter, target -> target.send(SseEmitter.event()
                    .name(CONNECTED_EVENT_NAME)
                    .id(String.valueOf(System.currentTimeMillis()))
                    .data("ok")));
        } catch (Exception e) {
            sseEmitterManager.completeWithErrorSafely(emitter, e);
            return emitter;
        }

        ScheduledFuture<?> beat = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                sseEmitterManager.trySendSafely(emitter, target -> target.send(SseEmitter.event()
                        .name(HEARTBEAT_EVENT_NAME)
                        .id(String.valueOf(System.currentTimeMillis()))
                        .data("ok")));
            } catch (Exception e) {
                sseEmitterManager.completeWithErrorSafely(emitter, e);
            }
        }, Duration.ofSeconds(15));
        beatHolder.set(beat);
        if (cancelled.get()) beat.cancel(true);

        return emitter;
    }
}
