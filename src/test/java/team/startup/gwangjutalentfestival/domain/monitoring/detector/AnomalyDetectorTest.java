package team.startup.gwangjutalentfestival.domain.monitoring.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.MlServerClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.PrometheusClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectorTest {

    @Mock
    private PrometheusClient prometheusClient;

    @Mock
    private DiscordWebhookClient discordWebhookClient;

    @Mock
    private AnomalyEventAppender anomalyEventAppender;

    @Mock
    private MlServerClient mlServerClient;

    @InjectMocks
    private AnomalyDetector anomalyDetector;

    private final AnomalyRule seatFailureRule = AnomalyRule.ALL.stream()
            .filter(r -> "seat".equals(r.domain()) && "failure_rate".equals(r.metricName()))
            .findFirst()
            .orElseThrow();

    @Test
    void ML_응답_성공시_MlScoreResult가_Appender에_전달됨() {
        double detectedValue = 0.08;
        MlAnomalyScoreResponse mlResponse = new MlAnomalyScoreResponse(0.0794, "anomaly", "iforest-v1", true);

        given(prometheusClient.query(seatFailureRule.promql())).willReturn(Optional.of(detectedValue));
        given(mlServerClient.call(any(MlAnomalyScoreRequest.class))).willReturn(Optional.of(mlResponse));
        given(anomalyEventAppender.appendIfNotDuplicate(eq(seatFailureRule), eq(detectedValue), any(MlScoreResult.class))).willReturn(true);

        anomalyDetector.detectAll();

        ArgumentCaptor<MlScoreResult> mlCaptor = ArgumentCaptor.forClass(MlScoreResult.class);
        verify(anomalyEventAppender).appendIfNotDuplicate(eq(seatFailureRule), eq(detectedValue), mlCaptor.capture());

        MlScoreResult captured = mlCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.anomalyScore()).isEqualTo(0.0794);
        assertThat(captured.modelVersion()).isEqualTo("iforest-v1");
        assertThat(captured.predictedLabel()).isEqualTo("anomaly");
    }

    @Test
    void ML_Optional_empty여도_rule_based_이벤트_저장과_Discord_알림_유지() {
        double detectedValue = 0.08;

        given(prometheusClient.query(seatFailureRule.promql())).willReturn(Optional.of(detectedValue));
        given(mlServerClient.call(any(MlAnomalyScoreRequest.class))).willReturn(Optional.empty());
        given(anomalyEventAppender.appendIfNotDuplicate(eq(seatFailureRule), eq(detectedValue), eq(null))).willReturn(true);

        anomalyDetector.detectAll();

        verify(anomalyEventAppender).appendIfNotDuplicate(eq(seatFailureRule), eq(detectedValue), eq(null));
        verify(discordWebhookClient).send(any(String.class));
    }

    @Test
    void Discord_메시지에_ML_결과_없으면_Rule_based_only_포함됨() {
        double detectedValue = 0.08;

        given(prometheusClient.query(seatFailureRule.promql())).willReturn(Optional.of(detectedValue));
        given(mlServerClient.call(any(MlAnomalyScoreRequest.class))).willReturn(Optional.empty());
        given(anomalyEventAppender.appendIfNotDuplicate(any(AnomalyRule.class), anyDouble(), eq(null))).willReturn(true);

        anomalyDetector.detectAll();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(discordWebhookClient).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("ML: Rule-based only");
    }

    @Test
    void Discord_메시지에_ML_결과_있으면_score와_modelVersion_포함됨() {
        double detectedValue = 0.08;
        MlAnomalyScoreResponse mlResponse = new MlAnomalyScoreResponse(0.0794, "anomaly", "iforest-v1", true);

        given(prometheusClient.query(seatFailureRule.promql())).willReturn(Optional.of(detectedValue));
        given(mlServerClient.call(any(MlAnomalyScoreRequest.class))).willReturn(Optional.of(mlResponse));
        given(anomalyEventAppender.appendIfNotDuplicate(any(AnomalyRule.class), anyDouble(), any(MlScoreResult.class))).willReturn(true);

        anomalyDetector.detectAll();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(discordWebhookClient).send(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains("ML Score: 0.0794 (anomaly)");
        assertThat(message).contains("Model Version: iforest-v1");
    }
}
