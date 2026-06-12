package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.MlServerClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.AppendAnomalyEventService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyDetector {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final PrometheusClient prometheusClient;
    private final DiscordWebhookClient discordWebhookClient;
    private final AppendAnomalyEventService appendAnomalyEventService;
    private final AnomalyEventRepository anomalyEventRepository;
    private final MlServerClient mlServerClient;

    public void detectAll() {
        for (AnomalyRule rule : AnomalyRule.ALL) {
            Optional<Double> result = prometheusClient.query(rule.promql());
            if (result.isEmpty()) {
                log.warn("Prometheus 쿼리 결과 없음. domain={}, metric={}", rule.domain(), rule.metricName());
                continue;
            }

            double value = result.get();
            if (value >= rule.threshold()) {
                if (anomalyEventRepository.existsByDomainAndMetricNameAndStatus(
                        rule.domain(), rule.metricName(), AnomalyEventStatus.OPEN)) {
                    continue;
                }

                MlScoreResult mlScoreResult = callMlServer(rule, value);

                boolean saved;
                try {
                    saved = appendAnomalyEventService.execute(rule, value, mlScoreResult);
                } catch (Exception e) {
                    log.warn("이상 이벤트 저장 실패. domain={}, metric={}, error={}", rule.domain(), rule.metricName(), e.getMessage());
                    continue;
                }

                if (saved) {
                    String message = buildMessage(rule, value, mlScoreResult);
                    discordWebhookClient.send(message);
                }
            }
        }
    }

    @Nullable
    private MlScoreResult callMlServer(AnomalyRule rule, double value) {
        Optional<String> mlMetricName = toMlMetricName(rule.metricName());
        if (mlMetricName.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now(ZONE_SEOUL);
        MlAnomalyScoreRequest mlRequest = new MlAnomalyScoreRequest(
                rule.domain().toUpperCase(Locale.ROOT),
                mlMetricName.get(),
                value,
                now.getHour(),
                now.getDayOfWeek().getValue()
        );

        return mlServerClient.call(mlRequest)
                .map(r -> new MlScoreResult(r.anomalyScore(), r.modelVersion(), r.predictedLabel()))
                .orElse(null);
    }

    private Optional<String> toMlMetricName(String metricName) {
        return switch (metricName) {
            case "failure_rate", "p95_duration" -> Optional.of(metricName);
            default -> {
                log.warn("알 수 없는 metricName, ML 호출 skip. metricName={}", metricName);
                yield Optional.empty();
            }
        };
    }

    private String buildMessage(AnomalyRule rule, double value, @Nullable MlScoreResult mlResult) {
        String base;
        if ("failure_rate".equals(rule.metricName())) {
            base = "🚫 [%s] %s 이상: %.2f%% >= %.2f%%\n(%s)".formatted(
                    rule.domain(),
                    rule.metricName(),
                    value * 100,
                    rule.threshold() * 100,
                    rule.reason()
            );
        } else {
            base = "🚫 [%s] %s 이상: %.2fs >= %.2fs\n(%s)".formatted(
                    rule.domain(),
                    rule.metricName(),
                    value,
                    rule.threshold(),
                    rule.reason()
            );
        }

        if (mlResult == null) {
            return base + "\nML: Rule-based only";
        }

        StringBuilder ml = new StringBuilder();
        ml.append("\nML Score: %.4f (%s)".formatted(mlResult.anomalyScore(), mlResult.predictedLabel()));
        if (mlResult.modelVersion() != null) {
            ml.append("\nModel Version: %s".formatted(mlResult.modelVersion()));
        }
        return base + ml;
    }
}
