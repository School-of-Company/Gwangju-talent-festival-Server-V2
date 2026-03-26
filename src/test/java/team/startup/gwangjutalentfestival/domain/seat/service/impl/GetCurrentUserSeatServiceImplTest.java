package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserSeatServiceImplTest {

    @InjectMocks
    private GetCurrentUserSeatServiceImpl getCurrentUserSeatService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private UserUtil userUtil;

    private UserEntity user() {
        return UserEntity.builder().id(1L).build();
    }

    private SeatEntity seat() {
        return SeatEntity.builder()
                .seatSection("B")
                .seatNumber(5)
                .user(user())
                .build();
    }

    @Test
    void 정상_좌석_조회_성공() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findByUser(any())).willReturn(Optional.of(seat()));

        GetSeatResponse response = getCurrentUserSeatService.execute();

        assertThat(response.seatSection()).isEqualTo("B");
        assertThat(response.seatNumber()).isEqualTo(5);
    }

    @Test
    void 예약된_좌석_없음_예외() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findByUser(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserSeatService.execute())
                .isInstanceOf(SeatNotFoundException.class);
    }
}
