package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetSeatsByPhoneNumberServiceImplTest {

    private static final String PHONE_NUMBER = "01012345678";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    private GetSeatsByPhoneNumberServiceImpl service() {
        return new GetSeatsByPhoneNumberServiceImpl(userRepository, seatReservationRepository);
    }

    private UserEntity user() {
        return UserEntity.builder()
                .id(USER_ID)
                .phoneNumber(PHONE_NUMBER)
                .role(Role.PERFORMER)
                .build();
    }

    private SeatEntity seat(String section, int number) {
        return SeatEntity.builder()
                .seatSection(section)
                .seatNumber(number)
                .build();
    }

    @Test
    void 전화번호로_예매한_좌석_목록을_반환한다() {
        given(userRepository.findByPhoneNumber(PHONE_NUMBER)).willReturn(Optional.of(user()));
        given(seatReservationRepository.findAllByUserId(USER_ID))
                .willReturn(List.of(seat("A", 16), seat("A", 17)));

        List<GetSeatResponse> result = service().execute(PHONE_NUMBER);

        assertThat(result).containsExactly(
                new GetSeatResponse("A", 16),
                new GetSeatResponse("A", 17));
    }

    @Test
    void 예매한_좌석이_없으면_빈_목록을_반환한다() {
        given(userRepository.findByPhoneNumber(PHONE_NUMBER)).willReturn(Optional.of(user()));
        given(seatReservationRepository.findAllByUserId(USER_ID)).willReturn(List.of());

        assertThat(service().execute(PHONE_NUMBER)).isEmpty();
    }

    @Test
    void 존재하지_않는_전화번호면_UserNotFoundException이_발생한다() {
        given(userRepository.findByPhoneNumber(PHONE_NUMBER)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().execute(PHONE_NUMBER))
                .isInstanceOf(UserNotFoundException.class);

        verify(seatReservationRepository, never()).findAllByUserId(USER_ID);
    }
}
