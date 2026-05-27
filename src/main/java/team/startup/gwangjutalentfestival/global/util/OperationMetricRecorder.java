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
                       long startNano, boolean success) {
        if (success && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    String counterName = (status == STATUS_COMMITTED) ? successCounterName : failureCounterName;
                    recordMetric(timerName, counterName, startNano);
                }
            });
        } else {
            recordMetric(timerName, success ? successCounterName : failureCounterName, startNano);
        }
    }

    public void record(String timerName, String successCounterName, String failureCounterName, Runnable action) {
        long start = System.nanoTime();
        try {
            action.run();
            record(timerName, successCounterName, failureCounterName, start, true);
        } catch (Exception e) {
            record(timerName, successCounterName, failureCounterName, start, false);
            throw e;
        }
    }

    private void recordMetric(String timerName, String counterName, long startNano) {
        try {
            meterRegistry.timer(timerName)
                    .record(System.nanoTime() - startNano, TimeUnit.NANOSECONDS);
            meterRegistry.counter(counterName).increment();
        } catch (Exception e) {
            log.warn("{} metric 기록 실패", timerName, e);
        }
    }
}