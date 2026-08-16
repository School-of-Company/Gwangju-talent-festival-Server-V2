package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationPeriodException;
import team.startup.gwangjutalentfestival.domain.seat.properties.SeatReservationPeriodProperties;
import team.startup.gwangjutalentfestival.domain.seat.properties.SeatReservationPeriodProperties.Period;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.util.UserUtil;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
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

    /** 현재 시각을 포함하는 기간 */
    private static final Period OPEN = new Period(
            LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.of(2099, 12, 31, 23, 59, 59));
    /** 이미 종료된 기간 */
    private static final Period CLOSED = new Period(
            LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.of(2000, 1, 2, 0, 0, 0));

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

    @ParameterizedTest
    @CsvSource({
            "A, 1", "A, 15",
            "B, 1", "B, 12",
            "C, 1", "C, 7", "C, 14", "C, 15",
            "E, 1", "E, 24", "E, 73", "E, 96"
    })
    void 정적_금지_좌석은_모든_역할의_예약을_거부한다(String section, int seatNumber) {
        SeatReservationValidator realValidator = new SeatReservationValidator(
                seatReservationRepository,
                seatBanRepository,
                userRepository,
                new SeatUtil(),
                new SeatReservationPeriodProperties(OPEN, OPEN)
        );

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            for (Role role : Role.values()) {
                userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(role);
                assertThatThrownBy(() -> realValidator.validateSeatAccess(section, seatNumber))
                        .isInstanceOf(SeatBannedException.class);
            }
        }
    }

    @Test
    void USER가_보유한_좌석과_신규_좌석의_합이_한도를_넘으면_예외가_발생한다() {
        assertThatThrownBy(() -> validator.validateReservationLimit(Role.USER, 1, 1))
                .isInstanceOf(SeatReservationLimitExceededException.class);
    }

    @Test
    void PERFORMER가_보유한_좌석과_신규_좌석의_합이_두_석을_넘으면_예외가_발생한다() {
        assertThatThrownBy(() -> validator.validateReservationLimit(Role.PERFORMER, 1, 2))
                .isInstanceOf(SeatReservationLimitExceededException.class);
    }

    @Test
    void PERFORMER_1석_보유시_추가_예약이_가능하다() {
        assertThatCode(() -> validator.validateReservationLimit(Role.PERFORMER, 1, 1))
                .doesNotThrowAnyException();
    }

    @Test
    void 현재_사용자_행을_잠가서_반환한다() {
        UserEntity user = UserEntity.builder().id(USER_ID).role(Role.PERFORMER).build();
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);
            given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));

            assertThat(validator.lockCurrentUser()).isSameAs(user);
        }
    }

    @Test
    void 잠글_현재_사용자가_없으면_UserNotFoundException이_발생한다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);
            given(userRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(validator::lockCurrentUser)
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Test
    void 참가자_기간이_지났으면_SeatReservationPeriodException이_발생한다() {
        SeatReservationValidator periodValidator = validatorWithPeriods(CLOSED, OPEN);

        assertThatThrownBy(() -> periodValidator.validateReservationPeriod(Role.PERFORMER))
                .isInstanceOf(SeatReservationPeriodException.class);
    }

    @Test
    void 일반_유저_기간이_아니면_SeatReservationPeriodException이_발생한다() {
        SeatReservationValidator periodValidator = validatorWithPeriods(OPEN, CLOSED);

        assertThatThrownBy(() -> periodValidator.validateReservationPeriod(Role.USER))
                .isInstanceOf(SeatReservationPeriodException.class);
    }

    @Test
    void 참가자는_일반_유저_기간이_열려있어도_예매할_수_없다() {
        SeatReservationValidator periodValidator = validatorWithPeriods(CLOSED, OPEN);

        assertThatThrownBy(() -> periodValidator.validateReservationPeriod(Role.PERFORMER))
                .isInstanceOf(SeatReservationPeriodException.class);
        assertThatCode(() -> periodValidator.validateReservationPeriod(Role.USER))
                .doesNotThrowAnyException();
    }

    @Test
    void 기간_안이면_예외가_발생하지_않는다() {
        SeatReservationValidator periodValidator = validatorWithPeriods(OPEN, OPEN);

        assertThatCode(() -> periodValidator.validateReservationPeriod(Role.PERFORMER))
                .doesNotThrowAnyException();
        assertThatCode(() -> periodValidator.validateReservationPeriod(Role.USER))
                .doesNotThrowAnyException();
    }

    private SeatReservationValidator validatorWithPeriods(Period performer, Period user) {
        return new SeatReservationValidator(
                seatReservationRepository,
                seatBanRepository,
                userRepository,
                seatUtil,
                new SeatReservationPeriodProperties(performer, user)
        );
    }
}
