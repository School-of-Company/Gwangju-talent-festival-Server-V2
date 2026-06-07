package team.startup.gwangjutalentfestival.domain.monitoring.detector;

public record MlScoreResult(Double anomalyScore, String modelVersion, String predictedLabel) {}
