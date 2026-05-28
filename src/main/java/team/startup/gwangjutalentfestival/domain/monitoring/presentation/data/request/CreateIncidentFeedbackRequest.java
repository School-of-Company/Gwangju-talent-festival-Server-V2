package team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;

public record CreateIncidentFeedbackRequest(
        @NotNull FeedbackLabel label,
        @Size(max = 500) String note
) {
}
