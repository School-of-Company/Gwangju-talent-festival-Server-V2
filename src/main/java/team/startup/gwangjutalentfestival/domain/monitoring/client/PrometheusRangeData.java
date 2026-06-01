package team.startup.gwangjutalentfestival.domain.monitoring.client;

import java.util.List;

public record PrometheusRangeData(List<PrometheusRangeSeries> result) {}