package team.startup.gwangjutalentfestival.domain.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventEntity;
import team.startup.gwangjutalentfestival.domain.monitoring.entity.AnomalyEventStatus;
import team.startup.gwangjutalentfestival.domain.monitoring.presentation.data.response.AnomalyEventListResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.repository.AnomalyEventRepository;
import team.startup.gwangjutalentfestival.domain.monitoring.service.impl.GetAnomalyEventListServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class GetAnomalyEventListServiceTest {

    @Mock
    private AnomalyEventRepository anomalyEventRepository;

    @InjectMocks
    private GetAnomalyEventListServiceImpl getAnomalyEventListService;

    @Test
    void status가_null이면_전체_조회_메서드가_호출된다() {
        given(anomalyEventRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        Page<AnomalyEventListResponse> result = getAnomalyEventListService.execute(0, 20, null);

        assertThat(result).isNotNull();
        verify(anomalyEventRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
        verifyNoMoreInteractions(anomalyEventRepository);
    }

    @Test
    void status가_있으면_필터_조회_메서드가_호출된다() {
        given(anomalyEventRepository.findAllByStatusOrderByCreatedAtDesc(eq(AnomalyEventStatus.OPEN), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        Page<AnomalyEventListResponse> result = getAnomalyEventListService.execute(0, 20, AnomalyEventStatus.OPEN);

        assertThat(result).isNotNull();
        verify(anomalyEventRepository).findAllByStatusOrderByCreatedAtDesc(eq(AnomalyEventStatus.OPEN), any(Pageable.class));
        verifyNoMoreInteractions(anomalyEventRepository);
    }

    @Test
    void 음수_page와_범위_초과_size는_방어_로직에_의해_보정된다() {
        given(anomalyEventRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        Page<AnomalyEventListResponse> result = getAnomalyEventListService.execute(-5, 200, null);

        assertThat(result).isNotNull();
        verify(anomalyEventRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }
}
