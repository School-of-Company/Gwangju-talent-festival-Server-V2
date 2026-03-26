package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Optional;

import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PerformerCancelSeatReservationServiceImplTest {

    @InjectMocks
    private PerformerCancelSeatReservationServiceImpl performerCancelSeatReservationService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private static final String SEAT_SECTION = "A";
    private static final Integer SEAT_NUMBER = 1;

    private PerformerCancelSeatReservationRequest request() {
        return new PerformerCancelSeatReservationRequest(SEAT_SECTION, SEAT_NUMBER);
    }

    private UserEntity user() {
        return UserEntity.builder().id(1L).build();
    }

    private SeatEntity seat() {
        return SeatEntity.builder()
                .seatSection(SEAT_SECTION)
                .seatNumber(SEAT_NUMBER)
                .user(user())
                .build();
    }

    @Test
    void 정상_예약_취소_성공() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findBySeatSectionAndSeatNumberAndUser(any(), any(), any()))
                .willReturn(Optional.of(seat()));

        performerCancelSeatReservationService.execute(request());

        verify(seatReservationRepository).delete(any(SeatEntity.class));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(SEAT_SECTION, SEAT_NUMBER, true));
    }

    @Test
    void 예약된_좌석_없음_예외() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findBySeatSectionAndSeatNumberAndUser(any(), any(), any()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> performerCancelSeatReservationService.execute(request()))
                .isInstanceOf(SeatNotFoundException.class);
    }
}
