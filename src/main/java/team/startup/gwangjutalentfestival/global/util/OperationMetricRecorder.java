package team.startup.gwangjutalentfestival.global.util;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationMetricRecorder {

    private final MeterRegistry meterRegistry;

    public void record(String timerName, String successCounterName, String failureCounterName,
                       long startNano, boolean success) {
        try {
            meterRegistry.timer(timerName)
                    .record(System.nanoTime() - startNano, TimeUnit.NANOSECONDS);
            meterRegistry.counter(success ? successCounterName : failureCounterName).increment();
        } catch (Exception e) {
            log.warn("{} metric 기록 실패", timerName, e);
        }
    }
}