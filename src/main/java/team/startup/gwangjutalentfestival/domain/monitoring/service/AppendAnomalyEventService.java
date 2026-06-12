package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.springframework.lang.Nullable;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.AnomalyRule;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.MlScoreResult;

public interface AppendAnomalyEventService {
    boolean execute(AnomalyRule rule, double detectedValue, @Nullable MlScoreResult mlResult);
}