package team.startup.gwangjutalentfestival.domain.monitoring.client;

import java.util.List;
import java.util.Map;

public record PrometheusRangeSeries(Map<String, String> metric, List<List<Object>> values) {}