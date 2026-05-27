package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnomalyEventAppender {

    private final AnomalyEventRepository anomalyEventRepository;

    @Transactional
    public boolean appendIfNotDuplicate(AnomalyRule rule, double detectedValue) {
        if (anomalyEventRepository.existsByDomainAndMetricNameAndStatus(
                rule.domain(), rule.metricName(), AnomalyEventStatus.OPEN)) {
            return false;
        }

        anomalyEventRepository.save(
                AnomalyEventEntity.builder()
                        .domain(rule.domain())
                        .metricName(rule.metricName())
                        .status(AnomalyEventStatus.OPEN)
                        .detectedValue(detectedValue)
                        .thresholdValue(rule.threshold())
                        .reason(rule.reason())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return true;
    }
}
