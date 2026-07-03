package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import java.util.Arrays;
import java.util.List;

public enum DatasetMetricQuery {

    SEAT_FAILURE_RATE(
            "seat",
            "failure_rate",
            "rate(seat_reservation_failure_total{application=\"{application}\"}[5m]) / (rate(seat_reservation_success_total{application=\"{application}\"}[5m]) + rate(seat_reservation_failure_total{application=\"{application}\"}[5m]))"
    ),
    SEAT_P95_DURATION(
            "seat",
            "p95_duration",
            "histogram_quantile(0.95, sum by (le) (rate(seat_reservation_duration_seconds_bucket{application=\"{application}\"}[5m])))"
    ),
    JUDGE_FAILURE_RATE(
            "judge",
            "failure_rate",
            "rate(judge_submit_failure_total{application=\"{application}\"}[5m]) / (rate(judge_submit_success_total{application=\"{application}\"}[5m]) + rate(judge_submit_failure_total{application=\"{application}\"}[5m]))"
    ),
    JUDGE_P95_DURATION(
            "judge",
            "p95_duration",
            "histogram_quantile(0.95, sum by (le) (rate(judge_submit_duration_seconds_bucket{application=\"{application}\"}[5m])))"
    );

    public static final List<DatasetMetricQuery> ALL = Arrays.asList(values());

    private final String domain;
    private final String metricName;
    private final String promql;

    DatasetMetricQuery(String domain, String metricName, String promql) {
        this.domain = domain;
        this.metricName = metricName;
        this.promql = promql;
    }

    public String getDomain() { return domain; }
    public String getMetricName() { return metricName; }
    public String getPromql() { return promql; }
}
