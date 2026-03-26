package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetByCurrentPerformerSeatsServiceImplTest {

    @InjectMocks
    private GetByCurrentPerformerSeatsServiceImpl getByCurrentPerformerSeatsService;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private UserUtil userUtil;

    private static final Long USER_ID = 1L;

    private UserEntity user() {
        return UserEntity.builder().id(USER_ID).build();
    }

    private SeatEntity seatOf(String section, Integer number) {
        return SeatEntity.builder()
                .seatSection(section)
                .seatNumber(number)
                .user(user())
                .build();
    }

    @Test
    void 정상_좌석_목록_조회_성공() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findAllByUserId(USER_ID))
                .willReturn(List.of(seatOf("A", 1), seatOf("B", 3)));

        List<GetSeatResponse> result = getByCurrentPerformerSeatsService.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).seatSection()).isEqualTo("A");
        assertThat(result.get(0).seatNumber()).isEqualTo(1);
        assertThat(result.get(1).seatSection()).isEqualTo("B");
        assertThat(result.get(1).seatNumber()).isEqualTo(3);
    }

    @Test
    void 예약된_좌석_없으면_빈_리스트_반환() {
        given(userUtil.getCurrentUser()).willReturn(user());
        given(seatReservationRepository.findAllByUserId(USER_ID)).willReturn(List.of());

        List<GetSeatResponse> result = getByCurrentPerformerSeatsService.execute();

        assertThat(result).isEmpty();
    }
}
