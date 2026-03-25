package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
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
class CancelSeatReservationServiceImplTest {

    @InjectMocks
    private CancelSeatReservationServiceImpl cancelSeatReservationService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UserEntity user() {
        return UserEntity.builder().id(1L).build();
    }

    private SeatEntity seat() {
        return SeatEntity.builder()
                .seatSection('A')
                .seatNumber(1)
                .user(user())
                .build();
    }

    @Test
    void 정상_예약_취소_성공() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findByUser(any())).willReturn(Optional.of(seat()));

        cancelSeatReservationService.execute();

        verify(seatReservationRepository).delete(any(SeatEntity.class));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent("A", 1, true));
    }

    @Test
    void 예약된_좌석_없음_예외() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findByUser(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> cancelSeatReservationService.execute())
                .isInstanceOf(SeatNotFoundException.class);
    }
}
