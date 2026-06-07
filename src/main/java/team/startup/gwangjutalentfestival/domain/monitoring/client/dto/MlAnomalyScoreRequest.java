package team.startup.gwangjutalentfestival.domain.monitoring.client.dto;

public record MlAnomalyScoreRequest(
        String domain,
        String metricName,
        double value,
        int hourOfDay,
        int dayOfWeek
) {}
