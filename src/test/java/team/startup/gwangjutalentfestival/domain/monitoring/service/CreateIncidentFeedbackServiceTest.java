package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.FeedbackLabel;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.IncidentFeedbackEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.AnomalyEventNotFoundException;
import team.startup.gwangjutalentfestival.domain.monitoring.exception.FeedbackAlreadyExistsException;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.request.CreateIncidentFeedbackRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.IncidentFeedbackRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.impl.CreateIncidentFeedbackServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateIncidentFeedbackServiceTest {

    private static final Long EVENT_ID = 1L;
    private static final Long NOT_FOUND_EVENT_ID = 99L;

    @Mock
    private AnomalyEventRepository anomalyEventRepository;

    @Mock
    private IncidentFeedbackRepository incidentFeedbackRepository;

    @InjectMocks
    private CreateIncidentFeedbackServiceImpl createIncidentFeedbackService;

    private AnomalyEventEntity openEvent;

    @BeforeEach
    void setUp() {
        openEvent = AnomalyEventEntity.builder()
                .id(EVENT_ID)
                .domain("seat")
                .metricName("failure_rate")
                .status(AnomalyEventStatus.OPEN)
                .detectedValue(7.0)
                .thresholdValue(5.0)
                .reason("좌석 예매 실패율이 기준치를 초과했습니다.")
                .build();
    }

    @Test
    void TRUE_INCIDENT_피드백_등록_시_anomaly_event가_RESOLVED_상태로_변경된다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(false);
        given(incidentFeedbackRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        createIncidentFeedbackService.execute(EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.TRUE_INCIDENT, null));

        assertThat(openEvent.getStatus()).isEqualTo(AnomalyEventStatus.RESOLVED);
        assertThat(openEvent.getResolvedAt()).isNotNull();
    }

    @Test
    void FALSE_POSITIVE_피드백_등록_시_anomaly_event가_RESOLVED_상태로_변경된다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(false);
        given(incidentFeedbackRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        createIncidentFeedbackService.execute(EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.FALSE_POSITIVE, "정상 트래픽 증가"));

        assertThat(openEvent.getStatus()).isEqualTo(AnomalyEventStatus.RESOLVED);
        assertThat(openEvent.getResolvedAt()).isNotNull();
    }

    @Test
    void IGNORED_피드백_등록_시_anomaly_event가_IGNORED_상태로_변경된다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(false);
        given(incidentFeedbackRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        createIncidentFeedbackService.execute(EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.IGNORED, null));

        assertThat(openEvent.getStatus()).isEqualTo(AnomalyEventStatus.IGNORED);
        assertThat(openEvent.getResolvedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_이벤트에_피드백_등록_시_AnomalyEventNotFoundException이_발생한다() {
        given(anomalyEventRepository.findById(NOT_FOUND_EVENT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> createIncidentFeedbackService.execute(
                NOT_FOUND_EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.TRUE_INCIDENT, null)
        )).isInstanceOf(AnomalyEventNotFoundException.class);
    }

    @Test
    void 이미_피드백이_존재하는_이벤트에_등록_시_FeedbackAlreadyExistsException이_발생한다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(true);

        assertThatThrownBy(() -> createIncidentFeedbackService.execute(
                EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.FALSE_POSITIVE, null)
        )).isInstanceOf(FeedbackAlreadyExistsException.class);
    }

    @Test
    void 동시_요청으로_DataIntegrityViolationException_발생_시_FeedbackAlreadyExistsException으로_변환된다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(false);
        given(incidentFeedbackRepository.save(any(IncidentFeedbackEntity.class)))
                .willThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> createIncidentFeedbackService.execute(
                EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.TRUE_INCIDENT, null)
        )).isInstanceOf(FeedbackAlreadyExistsException.class);
    }

    @Test
    void 피드백_등록_시_label과_note가_올바르게_저장된다() {
        given(anomalyEventRepository.findById(EVENT_ID)).willReturn(Optional.of(openEvent));
        given(incidentFeedbackRepository.existsByAnomalyEventId(EVENT_ID)).willReturn(false);
        ArgumentCaptor<IncidentFeedbackEntity> captor = ArgumentCaptor.forClass(IncidentFeedbackEntity.class);
        given(incidentFeedbackRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        createIncidentFeedbackService.execute(EVENT_ID, new CreateIncidentFeedbackRequest(FeedbackLabel.TRUE_INCIDENT, "실제 장애"));

        IncidentFeedbackEntity saved = captor.getValue();
        assertThat(saved.getLabel()).isEqualTo(FeedbackLabel.TRUE_INCIDENT);
        assertThat(saved.getNote()).isEqualTo("실제 장애");
        verify(incidentFeedbackRepository).save(any(IncidentFeedbackEntity.class));
    }
}
