package team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response;

public record AnomalyEventSummaryResponse(
        long totalEvents,
        long openEvents,
        long resolvedEvents,
        long ignoredEvents,
        long trueIncidentCount,
        long falsePositiveCount,
        double falsePositiveRate
) {
}
