package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.seat.exception.InvalidSeatSectionException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatBanCustomRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.custom.SeatReservationCustomRepository;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class GetSeatsBySectionServiceImplTest {

    @InjectMocks
    private GetSeatsBySectionServiceImpl getSeatsBySectionService;

    @Spy
    private SeatUtil seatUtil = new SeatUtil();

    @Mock
    private SeatReservationCustomRepository seatReservationCustomRepository;

    @Mock
    private SeatBanCustomRepository seatBanCustomRepository;

    private static final String SECTION = "A";

    @Test
    void 모든_좌석_예약_가능() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole(SECTION, Role.USER)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats()).hasSize(101);
            assertThat(response.seats()).containsOnly(true);
        }
    }

    @Test
    void 예약된_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of(2, 4));
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole(SECTION, Role.USER)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats().get(0)).isTrue();
            assertThat(response.seats().get(1)).isFalse(); // 2번
            assertThat(response.seats().get(2)).isTrue();
            assertThat(response.seats().get(3)).isFalse(); // 4번
            assertThat(response.seats().get(4)).isTrue();
        }
    }

    @Test
    void 차단된_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole(SECTION, Role.USER)).willReturn(Set.of(1, 3));

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats().get(0)).isFalse(); // 1번
            assertThat(response.seats().get(1)).isTrue();
            assertThat(response.seats().get(2)).isFalse(); // 3번
        }
    }

    @Test
    void W구역_전체_좌석_예약_가능() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("W")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole("W", Role.USER)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute("W");

            assertThat(response.seats()).hasSize(6);
            assertThat(response.seats()).containsOnly(true);
        }
    }

    @Test
    void B구역_전체_좌석_예약_가능() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("B")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole("B", Role.USER)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute("B");

            assertThat(response.seats()).hasSize(132);
            assertThat(response.seats()).containsOnly(true);
        }
    }

    @Test
    void 잘못된_구역_예외() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("Z")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSectionAndRole("Z", Role.USER)).willReturn(Set.of());

            assertThatThrownBy(() -> getSeatsBySectionService.execute("Z"))
                    .isInstanceOf(InvalidSeatSectionException.class);
        }
    }
}
