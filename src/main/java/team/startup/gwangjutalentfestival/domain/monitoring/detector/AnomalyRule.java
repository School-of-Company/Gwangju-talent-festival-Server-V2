package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import java.util.List;

public record AnomalyRule(
        String domain,
        String metricName,
        String promql,
        double threshold,
        String reason
) {
    public static final List<AnomalyRule> ALL = List.of(
            new AnomalyRule(
                    "seat",
                    "failure_rate",
                    "rate(seat_reservation_failure_total{application=\"{application}\"}[5m]) / (rate(seat_reservation_success_total{application=\"{application}\"}[5m]) + rate(seat_reservation_failure_total{application=\"{application}\"}[5m]))",
                    0.05,
                    "좌석 예매 실패율이 기준치를 초과했습니다."
            ),
            new AnomalyRule(
                    "seat",
                    "p95_duration",
                    "histogram_quantile(0.95, sum by (le) (rate(seat_reservation_duration_seconds_bucket{application=\"{application}\"}[5m])))",
                    2.5,
                    "좌석 예매 p95 응답 시간이 기준치를 초과했습니다."
            ),
            new AnomalyRule(
                    "judge",
                    "failure_rate",
                    "rate(judge_submit_failure_total{application=\"{application}\"}[5m]) / (rate(judge_submit_success_total{application=\"{application}\"}[5m]) + rate(judge_submit_failure_total{application=\"{application}\"}[5m]))",
                    0.03,
                    "심사 제출 실패율이 기준치를 초과했습니다."
            ),
            new AnomalyRule(
                    "judge",
                    "p95_duration",
                    "histogram_quantile(0.95, sum by (le) (rate(judge_submit_duration_seconds_bucket{application=\"{application}\"}[5m])))",
                    2.0,
                    "심사 제출 p95 응답 시간이 기준치를 초과했습니다."
            )
    );
}
