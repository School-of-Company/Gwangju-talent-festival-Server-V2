package team.startup.gwangjutalentfestival.domain.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PrometheusClient {

    private final RestClient prometheusRestClient;
    private final String applicationName;

    public PrometheusClient(
            @Qualifier("prometheusRestClient") RestClient prometheusRestClient,
            @Value("${spring.application.name}") String applicationName
    ) {
        this.prometheusRestClient = prometheusRestClient;
        this.applicationName = applicationName;
    }

    public Optional<Double> query(String promql) {
        String resolvedPromql = promql.replace("{application}", applicationName);
        try {
            PrometheusResponse response = prometheusRestClient.get()
                    .uri("/api/v1/query?query={promql}", resolvedPromql)
                    .retrieve()
                    .body(PrometheusResponse.class);

            if (response == null || !"success".equals(response.status())) {
                log.warn("Prometheus query 호출 실패 또는 에러 응답. status={}, promql={}",
                        response != null ? response.status() : "null", resolvedPromql);
                return Optional.empty();
            }

            if (response.data() == null || response.data().result() == null || response.data().result().isEmpty()) {
                log.debug("Prometheus query 결과가 비어있습니다. promql={}", resolvedPromql);
                return Optional.empty();
            }

            PrometheusResult firstResult = response.data().result().get(0);
            if (firstResult == null || firstResult.value() == null || firstResult.value().size() < 2) {
                log.warn("Prometheus query 결과의 value 형식이 올바르지 않습니다. promql={}", resolvedPromql);
                return Optional.empty();
            }

            List<Object> valueList = firstResult.value();
            String rawValue = valueList.get(1).toString();
            double value = Double.parseDouble(rawValue);

            if (Double.isNaN(value) || Double.isInfinite(value)) {
                log.debug("Prometheus query 값이 NaN 또는 Infinite입니다. promql={}, value={}", resolvedPromql, rawValue);
                return Optional.empty();
            }

            return Optional.of(value);
        } catch (Exception e) {
            log.warn("Prometheus query 실패. promql={}", resolvedPromql, e);
            return Optional.empty();
        }
    }

    public List<PrometheusRangePoint> queryRange(String promql, long start, long end, int stepSeconds) {
        String resolvedPromql = promql.replace("{application}", applicationName);
        try {
            PrometheusRangeResponse response = prometheusRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/query_range")
                            .queryParam("query", resolvedPromql)
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("step", stepSeconds)
                            .build())
                    .retrieve()
                    .body(PrometheusRangeResponse.class);

            if (response == null || !"success".equals(response.status())) {
                log.warn("Prometheus queryRange 호출 실패 또는 에러 응답. promql={}", resolvedPromql);
                return List.of();
            }

            if (response.data() == null || response.data().result() == null || response.data().result().isEmpty()) {
                log.debug("Prometheus queryRange 결과가 비어있습니다. promql={}", resolvedPromql);
                return List.of();
            }

            List<PrometheusRangePoint> points = new ArrayList<>();
            for (PrometheusRangeSeries series : response.data().result()) {
                if (series.values() == null) continue;
                for (List<Object> pair : series.values()) {
                    if (pair == null || pair.size() < 2) continue;
                    try {
                        long timestamp = ((Number) pair.get(0)).longValue();
                        double value = Double.parseDouble(pair.get(1).toString());
                        if (Double.isNaN(value) || Double.isInfinite(value)) {
                            log.debug("Prometheus queryRange 값이 NaN 또는 Infinite. promql={}, timestamp={}", resolvedPromql, timestamp);
                            continue;
                        }
                        points.add(new PrometheusRangePoint(timestamp, value));
                    } catch (Exception e) {
                        log.debug("Prometheus queryRange 포인트 파싱 실패. pair={}", pair);
                    }
                }
            }
            return points;
        } catch (Exception e) {
            log.warn("Prometheus queryRange 실패. promql={}", resolvedPromql, e);
            return List.of();
        }
    }

    record PrometheusResponse(String status, PrometheusData data) {}

    record PrometheusData(List<PrometheusResult> result) {}

    record PrometheusResult(List<Object> value) {}
}
