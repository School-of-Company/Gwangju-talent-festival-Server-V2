package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;

@Service
@RequiredArgsConstructor
public class AnomalyEventAppender {

    private final AnomalyEventRepository anomalyEventRepository;

    public boolean hasOpenEvent(AnomalyRule rule) {
        return anomalyEventRepository.existsByDomainAndMetricNameAndStatus(
                rule.domain(), rule.metricName(), AnomalyEventStatus.OPEN);
    }

    @Transactional
    public boolean appendIfNotDuplicate(AnomalyRule rule, double detectedValue, @Nullable MlScoreResult mlResult) {
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
                        .anomalyScore(mlResult != null ? mlResult.anomalyScore() : null)
                        .modelVersion(mlResult != null ? mlResult.modelVersion() : null)
                        .predictedLabel(mlResult != null ? mlResult.predictedLabel() : null)
                        .build()
        );

        return true;
    }
}
