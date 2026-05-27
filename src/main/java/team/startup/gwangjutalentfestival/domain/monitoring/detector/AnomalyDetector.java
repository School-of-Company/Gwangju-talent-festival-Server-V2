package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusClient;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyDetector {

    private final PrometheusClient prometheusClient;
    private final DiscordWebhookClient discordWebhookClient;
    private final AnomalyEventAppender anomalyEventAppender;

    public void detectAll() {
        for (AnomalyRule rule : AnomalyRule.ALL) {
            Optional<Double> result = prometheusClient.query(rule.promql());
            if (result.isEmpty()) {
                log.warn("Prometheus 쿼리 결과 없음. domain={}, metric={}", rule.domain(), rule.metricName());
                continue;
            }

            double value = result.get();
            if (value >= rule.threshold()) {
                boolean saved;
                try {
                    saved = anomalyEventAppender.appendIfNotDuplicate(rule, value);
                } catch (Exception e) {
                    log.warn("이상 이벤트 저장 실패. domain={}, metric={}, error={}", rule.domain(), rule.metricName(), e.getMessage());
                    continue;
                }

                if (saved) {
                    String message = buildMessage(rule, value);
                    discordWebhookClient.send(message);
                }
            }
        }
    }

    private String buildMessage(AnomalyRule rule, double value) {
        if ("failure_rate".equals(rule.metricName())) {
            return "🚫 [%s] %s 이상: %.2f%% >= %.2f%%\n(%s)".formatted(
                    rule.domain(),
                    rule.metricName(),
                    value * 100,
                    rule.threshold() * 100,
                    rule.reason()
            );
        }
        return "🚫 [%s] %s 이상: %.2fs >= %.2fs\n(%s)".formatted(
                rule.domain(),
                rule.metricName(),
                value,
                rule.threshold(),
                rule.reason()
        );
    }
}
