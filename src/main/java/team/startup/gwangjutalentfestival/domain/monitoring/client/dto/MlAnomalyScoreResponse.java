package team.startup.gwangjutalentfestival.domain.monitoring.client.dto;

public record MlAnomalyScoreResponse(
        Double anomalyScore,
        String predictedLabel,
        String modelVersion,
        boolean modelLoaded
) {}
