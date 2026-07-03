package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventSummaryResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.impl.GetAnomalyEventSummaryServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAnomalyEventSummaryServiceTest {

    @Mock
    private AnomalyEventRepository anomalyEventRepository;

    @Mock
    private IncidentFeedbackRepository incidentFeedbackRepository;

    @InjectMocks
    private GetAnomalyEventSummaryServiceImpl getAnomalyEventSummaryService;

    @Test
    void 정상_summary_반환_및_falsePositiveRate_계산이_올바르다() {
        given(anomalyEventRepository.count()).willReturn(10L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.OPEN)).willReturn(3L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.RESOLVED)).willReturn(5L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.IGNORED)).willReturn(2L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.TRUE_INCIDENT)).willReturn(3L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.FALSE_POSITIVE)).willReturn(2L);

        AnomalyEventSummaryResponse result = getAnomalyEventSummaryService.execute();

        assertThat(result.totalEvents()).isEqualTo(10L);
        assertThat(result.openEvents()).isEqualTo(3L);
        assertThat(result.resolvedEvents()).isEqualTo(5L);
        assertThat(result.ignoredEvents()).isEqualTo(2L);
        assertThat(result.trueIncidentCount()).isEqualTo(3L);
        assertThat(result.falsePositiveCount()).isEqualTo(2L);
        assertThat(result.falsePositiveRate()).isCloseTo(0.4, offset(0.001));
    }

    @Test
    void trueIncidentCount와_falsePositiveCount가_모두_0이면_falsePositiveRate는_0이다() {
        given(anomalyEventRepository.count()).willReturn(5L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.OPEN)).willReturn(5L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.RESOLVED)).willReturn(0L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.IGNORED)).willReturn(0L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.TRUE_INCIDENT)).willReturn(0L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.FALSE_POSITIVE)).willReturn(0L);

        AnomalyEventSummaryResponse result = getAnomalyEventSummaryService.execute();

        assertThat(result.falsePositiveRate()).isEqualTo(0.0);
    }

    @Test
    void falsePositive만_존재하면_falsePositiveRate는_1이다() {
        given(anomalyEventRepository.count()).willReturn(3L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.OPEN)).willReturn(0L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.RESOLVED)).willReturn(3L);
        given(anomalyEventRepository.countByStatus(AnomalyEventStatus.IGNORED)).willReturn(0L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.TRUE_INCIDENT)).willReturn(0L);
        given(incidentFeedbackRepository.countByLabel(FeedbackLabel.FALSE_POSITIVE)).willReturn(3L);

        AnomalyEventSummaryResponse result = getAnomalyEventSummaryService.execute();

        assertThat(result.falsePositiveRate()).isCloseTo(1.0, offset(0.001));
    }
}
