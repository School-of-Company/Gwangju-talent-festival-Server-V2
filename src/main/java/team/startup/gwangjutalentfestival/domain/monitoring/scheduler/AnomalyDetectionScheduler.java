package team.startup.gwangjutalentfestival.domain.monitoring.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.AnomalyDetector;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "monitoring.anomaly", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AnomalyDetectionScheduler {

    private final AnomalyDetector anomalyDetector;

    @Scheduled(fixedDelay = 60_000)
    public void execute() {
        try {
            anomalyDetector.detectAll();
        } catch (Exception e) {
            log.error("anomaly detection 실행 실패", e);
        }
    }
}
