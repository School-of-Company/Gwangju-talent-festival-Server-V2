package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.util.UserUtil;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SeatReservationValidatorTest {

    @InjectMocks
    private SeatReservationValidator validator;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private SeatBanRepository seatBanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatUtil seatUtil;

    private static final String SECTION = "A";
    private static final Integer SEAT_NUMBER = 1;
    private static final Integer MAX_SEATS = 101;
    private static final long USER_ID = 1L;

    @Test
    void 좌석_번호가_1_미만이면_SeatNotExistsInSectionException이_발생한다() {
        assertThatThrownBy(() -> validator.validateSeatRange(0, MAX_SEATS))
                .isInstanceOf(SeatNotExistsInSectionException.class);
    }

    @Test
    void 좌석_번호가_최대값_초과면_SeatNotExistsInSectionException이_발생한다() {
        assertThatThrownBy(() -> validator.validateSeatRange(MAX_SEATS + 1, MAX_SEATS))
                .isInstanceOf(SeatNotExistsInSectionException.class);
    }

    @Test
    void 이미_예약된_좌석이면_SeatAlreadyReservedException이_발생한다() {
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SECTION, SEAT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> validator.validateSeatAvailability(SECTION, SEAT_NUMBER))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    @Test
    void 차단된_좌석이면_SeatBannedException이_발생한다() {
        given(seatReservationRepository.existsBySeatSectionAndSeatNumber(SECTION, SEAT_NUMBER)).willReturn(false);
        given(seatBanRepository.existsBySeatSectionAndSeatNumber(SECTION, SEAT_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> validator.validateSeatAvailability(SECTION, SEAT_NUMBER))
                .isInstanceOf(SeatBannedException.class);
    }

    @Test
    void 현재_역할에_허용되지_않은_좌석이면_SeatBannedException이_발생한다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);
            given(seatUtil.isAllowedForRole(Role.PERFORMER, SECTION, SEAT_NUMBER)).willReturn(false);

            assertThatThrownBy(() -> validator.validateSeatAccess(SECTION, SEAT_NUMBER))
                    .isInstanceOf(SeatBannedException.class);
        }
    }

    @Test
    void USER_예약_한도_초과시_SeatReservationLimitExceededException이_발생한다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            UserEntity user = UserEntity.builder().id(USER_ID).role(Role.USER).build();
            userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));
            given(seatReservationRepository.countByUserId(USER_ID)).willReturn(1L);

            assertThatThrownBy(() -> validator.validateReservationLimit())
                    .isInstanceOf(SeatReservationLimitExceededException.class);
        }
    }

    @Test
    void PERFORMER_예약_한도_초과시_SeatReservationLimitExceededException이_발생한다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            UserEntity user = UserEntity.builder().id(USER_ID).role(Role.PERFORMER).build();
            userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);
            given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));
            given(seatReservationRepository.countByUserId(USER_ID)).willReturn(2L);

            assertThatThrownBy(() -> validator.validateReservationLimit())
                    .isInstanceOf(SeatReservationLimitExceededException.class);
        }
    }

    @Test
    void PERFORMER_1석_보유시_추가_예약이_가능하다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            UserEntity user = UserEntity.builder().id(USER_ID).role(Role.PERFORMER).build();
            userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);
            given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));
            given(seatReservationRepository.countByUserId(USER_ID)).willReturn(1L);

            assertThatCode(validator::validateReservationLimit).doesNotThrowAnyException();
        }
    }
}
