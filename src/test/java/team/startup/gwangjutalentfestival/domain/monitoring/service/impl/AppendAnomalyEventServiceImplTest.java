package team.startup.gwangjutalentfestival.domain.monitoring.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.AnomalyRule;
import team.startup.gwangjutalentfestival.domain.monitoring.detector.MlScoreResult;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppendAnomalyEventServiceImplTest {

    @Mock
    private AnomalyEventRepository anomalyEventRepository;

    @InjectMocks
    private AppendAnomalyEventServiceImpl appendAnomalyEventService;

    private final AnomalyRule rule = AnomalyRule.ALL.stream()
            .filter(r -> "seat".equals(r.domain()) && "failure_rate".equals(r.metricName()))
            .findFirst()
            .orElseThrow();

    @Test
    void ML_결과_있으면_anomalyScore_modelVersion_predictedLabel_저장() {
        given(anomalyEventRepository.existsByDomainAndMetricNameAndStatus("seat", "failure_rate", AnomalyEventStatus.OPEN))
                .willReturn(false);
        MlScoreResult mlResult = new MlScoreResult(0.0794, "iforest-v1", "anomaly");

        boolean saved = appendAnomalyEventService.execute(rule, 0.08, mlResult);

        assertThat(saved).isTrue();
        ArgumentCaptor<AnomalyEventEntity> entityCaptor = ArgumentCaptor.forClass(AnomalyEventEntity.class);
        verify(anomalyEventRepository).save(entityCaptor.capture());

        AnomalyEventEntity entity = entityCaptor.getValue();
        assertThat(entity.getAnomalyScore()).isEqualTo(0.0794);
        assertThat(entity.getModelVersion()).isEqualTo("iforest-v1");
        assertThat(entity.getPredictedLabel()).isEqualTo("anomaly");
    }

    @Test
    void ML_결과_없으면_세_필드_null_저장() {
        given(anomalyEventRepository.existsByDomainAndMetricNameAndStatus("seat", "failure_rate", AnomalyEventStatus.OPEN))
                .willReturn(false);

        boolean saved = appendAnomalyEventService.execute(rule, 0.08, null);

        assertThat(saved).isTrue();
        ArgumentCaptor<AnomalyEventEntity> entityCaptor = ArgumentCaptor.forClass(AnomalyEventEntity.class);
        verify(anomalyEventRepository).save(entityCaptor.capture());

        AnomalyEventEntity entity = entityCaptor.getValue();
        assertThat(entity.getAnomalyScore()).isNull();
        assertThat(entity.getModelVersion()).isNull();
        assertThat(entity.getPredictedLabel()).isNull();
    }

    @Test
    void 동일_domain_metric_OPEN_이벤트가_있으면_저장_안_함() {
        given(anomalyEventRepository.existsByDomainAndMetricNameAndStatus("seat", "failure_rate", AnomalyEventStatus.OPEN))
                .willReturn(true);

        boolean saved = appendAnomalyEventService.execute(rule, 0.08, null);

        assertThat(saved).isFalse();
    }
}
