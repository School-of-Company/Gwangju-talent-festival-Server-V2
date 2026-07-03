package team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response;

import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;

import java.time.LocalDateTime;

public record AnomalyEventDetailResponse(
        Long id,
        String domain,
        String metricName,
        AnomalyEventStatus status,
        Double detectedValue,
        Double thresholdValue,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        IncidentFeedbackResponse feedback
) {
}
