package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBanNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatLockRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancelSeatBanServiceImplTest {

    private static final String SECTION = "A";
    private static final int NUMBER = 1;
    private static final CancelSeatBanRequest REQUEST = new CancelSeatBanRequest(SECTION, NUMBER);

    @InjectMocks
    private CancelSeatBanServiceImpl service;

    @Mock
    private SeatBanRepository seatBanRepository;

    @Mock
    private SeatLockRepository seatLockRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void 차단_해제는_좌석을_잠근_뒤_삭제하고_이벤트를_발행한다() {
        SeatBanEntity ban = SeatBanEntity.builder().seatSection(SECTION).seatNumber(NUMBER).build();
        given(seatBanRepository.findBySeatSectionAndSeatNumber(SECTION, NUMBER)).willReturn(Optional.of(ban));

        service.execute(REQUEST);

        verify(seatLockRepository).lock(SECTION, NUMBER);
        verify(seatBanRepository).delete(ban);
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(SECTION, NUMBER, true));
    }

    @Test
    void 차단되지_않은_좌석은_해제할_수_없다() {
        given(seatBanRepository.findBySeatSectionAndSeatNumber(SECTION, NUMBER)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(REQUEST)).isInstanceOf(SeatBanNotFoundException.class);

        verify(seatBanRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }
}
