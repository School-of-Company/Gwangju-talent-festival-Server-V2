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
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats()).hasSize(101);
            assertThat(response.seats()).containsOnly(true);
        }
    }

    @Test
    void 예약된_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of(2, 4));
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());

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
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of(1, 3));

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats().get(0)).isFalse(); // 1번
            assertThat(response.seats().get(1)).isTrue();
            assertThat(response.seats().get(2)).isFalse(); // 3번
        }
    }

    @Test
    void W구역은_조회할_수_없다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("W")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection("W")).willReturn(Set.of());

            assertThatThrownBy(() -> getSeatsBySectionService.execute("W"))
                    .isInstanceOf(InvalidSeatSectionException.class);
        }
    }

    @Test
    void B구역_전체_좌석_예약_가능() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("B")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection("B")).willReturn(Set.of());

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
            given(seatBanCustomRepository.findSeatNumbersBySeatSection("Z")).willReturn(Set.of());

            assertThatThrownBy(() -> getSeatsBySectionService.execute("Z"))
                    .isInstanceOf(InvalidSeatSectionException.class);
        }
    }

    @Test
    void 공연자와_일반인의_좌석_범위가_분리된다() {
        given(seatReservationCustomRepository.findSeatNumbersBySeatSection("A")).willReturn(Set.of());
        given(seatBanCustomRepository.findSeatNumbersBySeatSection("A")).willReturn(Set.of());

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);
            GetSeatsBySectionResponse performer = getSeatsBySectionService.execute("A");
            assertThat(performer.seats().get(14)).isTrue();  // A15
            assertThat(performer.seats().get(15)).isFalse(); // A16
            assertThat(performer.seats().get(20)).isTrue();  // A21
            assertThat(performer.seats().get(23)).isFalse(); // A24
        }

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            GetSeatsBySectionResponse user = getSeatsBySectionService.execute("A");
            assertThat(user.seats().get(14)).isFalse();
            assertThat(user.seats().get(15)).isTrue();
            assertThat(user.seats().get(20)).isFalse();
            assertThat(user.seats().get(23)).isTrue();
        }
    }

    @Test
    void 관리자_밴은_일반인과_공연자에게_모두_적용된다() {
        given(seatReservationCustomRepository.findSeatNumbersBySeatSection("A")).willReturn(Set.of());
        given(seatBanCustomRepository.findSeatNumbersBySeatSection("A")).willReturn(Set.of(1, 16));

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);
            assertThat(getSeatsBySectionService.execute("A").seats().get(0)).isFalse();
        }

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            assertThat(getSeatsBySectionService.execute("A").seats().get(15)).isFalse();
        }
    }
}
