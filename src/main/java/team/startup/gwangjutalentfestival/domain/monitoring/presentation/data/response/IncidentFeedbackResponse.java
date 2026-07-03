package team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response;

import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;

import java.time.LocalDateTime;

public record IncidentFeedbackResponse(
        Long id,
        FeedbackLabel label,
        String note,
        LocalDateTime createdAt
) {
}
