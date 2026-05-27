package team.startup.gwangjutalentfestival.global.util;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationMetricRecorder {

    private final MeterRegistry meterRegistry;

    public void record(String timerName, String successCounterName, String failureCounterName,
                       long durationNano, boolean success) {
        if (success && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    String counterName = (status == STATUS_COMMITTED) ? successCounterName : failureCounterName;
                    recordMetric(timerName, counterName, durationNano);
                }
            });
        } else {
            recordMetric(timerName, success ? successCounterName : failureCounterName, durationNano);
        }
    }

    public void record(String timerName, String successCounterName, String failureCounterName, Runnable action) {
        long start = System.nanoTime();
        boolean success = false;
        try {
            action.run();
            success = true;
        } finally {
            long durationNano = System.nanoTime() - start;
            record(timerName, successCounterName, failureCounterName, durationNano, success);
        }
    }

    private void recordMetric(String timerName, String counterName, long durationNano) {
        try {
            meterRegistry.timer(timerName)
                    .record(durationNano, TimeUnit.NANOSECONDS);
            meterRegistry.counter(counterName).increment();
        } catch (Exception e) {
            log.warn("{} metric 기록 실패", timerName, e);
        }
    }
}