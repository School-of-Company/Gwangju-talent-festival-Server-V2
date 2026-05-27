package team.startup.gwangjutalentfestival.domain.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PrometheusClient {

    private final RestClient prometheusRestClient;
    private final MonitoringProperties monitoringProperties;

    public PrometheusClient(
            @Qualifier("prometheusRestClient") RestClient prometheusRestClient,
            MonitoringProperties monitoringProperties
    ) {
        this.prometheusRestClient = prometheusRestClient;
        this.monitoringProperties = monitoringProperties;
    }

    public Optional<Double> query(String promql) {
        try {
            PrometheusResponse response = prometheusRestClient.get()
                    .uri(monitoringProperties.prometheusBaseUrl() + "/api/v1/query?query={promql}", promql)
                    .retrieve()
                    .body(PrometheusResponse.class);

            if (response == null || response.data() == null || response.data().result() == null || response.data().result().isEmpty()) {
                log.warn("Prometheus query 결과가 비어있습니다. promql={}", promql);
                return Optional.empty();
            }

            String rawValue = response.data().result().get(0).value().get(1).toString();
            double value = Double.parseDouble(rawValue);

            if (Double.isNaN(value) || Double.isInfinite(value)) {
                log.warn("Prometheus query 값이 NaN 또는 Infinite입니다. promql={}, value={}", promql, rawValue);
                return Optional.empty();
            }

            return Optional.of(value);
        } catch (Exception e) {
            log.warn("Prometheus query 실패. promql={}, error={}", promql, e.getMessage());
            return Optional.empty();
        }
    }

    record PrometheusResponse(String status, PrometheusData data) {}

    record PrometheusData(List<PrometheusResult> result) {}

    record PrometheusResult(List<Object> value) {}
}
