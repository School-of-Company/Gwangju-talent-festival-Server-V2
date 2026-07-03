package team.startup.gwangjutalentfestival.domain.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MlProperties;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class MlServerClient {

    private static final Set<String> VALID_LABELS = Set.of("anomaly", "normal");

    private final RestClient mlRestClient;
    private final MlProperties mlProperties;

    public MlServerClient(
            @Qualifier("mlRestClient") RestClient mlRestClient,
            MlProperties mlProperties
    ) {
        this.mlRestClient = mlRestClient;
        this.mlProperties = mlProperties;
    }

    public Optional<MlAnomalyScoreResponse> call(MlAnomalyScoreRequest request) {
        if (!mlProperties.enabled() || mlProperties.baseUrl().isBlank()) {
            return Optional.empty();
        }

        try {
            MlAnomalyScoreResponse response = mlRestClient.post()
                    .uri(normalizeBaseUrl(mlProperties.baseUrl()) + "/anomaly-score")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlAnomalyScoreResponse.class);

            return validate(response);
        } catch (Exception e) {
            log.warn("ML Server 호출 실패 — rule-based 탐지는 계속 진행. error={}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<MlAnomalyScoreResponse> validate(MlAnomalyScoreResponse response) {
        if (response == null) {
            return Optional.empty();
        }
        if (!response.modelLoaded()) {
            return Optional.empty();
        }
        if (response.anomalyScore() == null) {
            return Optional.empty();
        }
        if (response.predictedLabel() == null || !VALID_LABELS.contains(response.predictedLabel())) {
            log.warn("ML Server 응답의 predictedLabel이 유효하지 않음. predictedLabel={}", response.predictedLabel());
            return Optional.empty();
        }
        if (response.modelVersion() == null) {
            log.warn("ML Server 응답의 modelVersion이 null입니다.");
        }
        return Optional.of(response);
    }

    private String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.strip();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
