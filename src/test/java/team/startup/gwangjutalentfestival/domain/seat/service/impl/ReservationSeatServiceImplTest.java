package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationSeatServiceImplTest {

    @InjectMocks
    private ReservationSeatServiceImpl reservationSeatService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private SeatBanRepository seatBanRepository;

    @Mock
    private SeatUtil seatUtil;

    @Mock
    private UserUtil userUtil;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private static final Character SEAT_SECTION = 'A';
    private static final Integer SEAT_NUMBER = 1;
    private static final Integer MAX_SEATS = 77;

    private ReservationSeatRequest request() {
        return new ReservationSeatRequest(String.valueOf(SEAT_SECTION), SEAT_NUMBER);
    }

    private UserEntity userOf(Role role) {
        return UserEntity.builder()
                .id(1L)
                .role(role)
                .build();
    }

    @Test
    void USER_정상_예약_성공() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willReturn(userOf(Role.USER));
        given(seatReservationRepository.countByUser(any())).willReturn(0L);

        reservationSeatService.execute(request());

        verify(seatReservationRepository).saveAndFlush(any(SeatEntity.class));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(String.valueOf(SEAT_SECTION), SEAT_NUMBER, false));
    }

    @Test
    void PERFORMER_정상_예약_성공() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willReturn(userOf(Role.PERFORMER));
        given(seatReservationRepository.countByUser(any())).willReturn(2L);

        reservationSeatService.execute(request());

        verify(seatReservationRepository).saveAndFlush(any(SeatEntity.class));
        verify(applicationEventPublisher).publishEvent(new SeatChangeEvent(String.valueOf(SEAT_SECTION), SEAT_NUMBER, false));
    }

    @Test
    void 범위_밖_좌석번호_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        ReservationSeatRequest outOfRange = new ReservationSeatRequest(String.valueOf(SEAT_SECTION), MAX_SEATS + 1);

        assertThatThrownBy(() -> reservationSeatService.execute(outOfRange))
                .isInstanceOf(SeatNotExistsInSectionException.class);
    }

    @Test
    void 이미_예약된_좌석_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    @Test
    void 밴된_좌석_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatBannedException.class);
    }

    @Test
    void 유저_없음_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willThrow(UserNotFoundException.class);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void USER_예약_한도_초과_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willReturn(userOf(Role.USER));
        given(seatReservationRepository.countByUser(any())).willReturn(1L);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatReservationLimitExceededException.class);
    }

    @Test
    void PERFORMER_예약_한도_초과_예외() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willReturn(userOf(Role.PERFORMER));
        given(seatReservationRepository.countByUser(any())).willReturn(3L);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatReservationLimitExceededException.class);
    }

    @Test
    void 동시_예약으로_DataIntegrityViolation_발생시_SeatAlreadyReservedException() {
        given(seatUtil.getMaxSeats(SEAT_SECTION)).willReturn(MAX_SEATS);
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SEAT_SECTION, SEAT_NUMBER)).willReturn(false);
        given(userUtil.getCurrentUser()).willReturn(userOf(Role.USER));
        given(seatReservationRepository.countByUser(any())).willReturn(0L);
        given(seatReservationRepository.saveAndFlush(any())).willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> reservationSeatService.execute(request()))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }
}
