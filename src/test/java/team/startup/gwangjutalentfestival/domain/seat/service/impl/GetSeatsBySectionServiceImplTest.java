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
import static org.mockito.ArgumentMatchers.anyString;
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
    void 관리자_조회에도_정적_금지_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats()).hasSize(101);
            assertThat(response.seats().subList(0, 15)).containsOnly(false);
            assertThat(response.seats().get(15)).isTrue(); // A16
        }
    }

    @Test
    void 예약된_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of(20, 22));
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats().get(18)).isTrue();
            assertThat(response.seats().get(19)).isFalse(); // 20번
            assertThat(response.seats().get(20)).isTrue();
            assertThat(response.seats().get(21)).isFalse(); // 22번
            assertThat(response.seats().get(22)).isTrue();
        }
    }

    @Test
    void 차단된_좌석은_false() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection(SECTION)).willReturn(Set.of(20, 22));

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute(SECTION);

            assertThat(response.seats().get(18)).isTrue();
            assertThat(response.seats().get(19)).isFalse(); // 20번
            assertThat(response.seats().get(20)).isTrue();
            assertThat(response.seats().get(21)).isFalse(); // 22번
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
    void B구역의_정적_금지_좌석을_반영한다() {
        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.ADMIN);
            given(seatReservationCustomRepository.findSeatNumbersBySeatSection("B")).willReturn(Set.of());
            given(seatBanCustomRepository.findSeatNumbersBySeatSection("B")).willReturn(Set.of());

            GetSeatsBySectionResponse response = getSeatsBySectionService.execute("B");

            assertThat(response.seats()).hasSize(132);
            assertThat(response.seats().subList(0, 12)).containsOnly(false);
            assertThat(response.seats().get(12)).isTrue(); // B13
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
            assertThat(performer.seats().get(14)).isFalse(); // A15
            assertThat(performer.seats().get(15)).isTrue();  // A16
            assertThat(performer.seats().get(31)).isTrue();  // A32
            assertThat(performer.seats().get(32)).isFalse(); // A33
        }

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.USER);
            GetSeatsBySectionResponse user = getSeatsBySectionService.execute("A");
            assertThat(user.seats().get(14)).isFalse();
            assertThat(user.seats().get(15)).isFalse();
            assertThat(user.seats().get(31)).isFalse();
            assertThat(user.seats().get(32)).isTrue();
        }
    }

    @Test
    void E구역의_정적_금지_좌석을_반영한다() {
        given(seatReservationCustomRepository.findSeatNumbersBySeatSection("E")).willReturn(Set.of());
        given(seatBanCustomRepository.findSeatNumbersBySeatSection("E")).willReturn(Set.of());

        for (Role role : Set.of(Role.USER, Role.ADMIN)) {
            try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
                userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(role);
                GetSeatsBySectionResponse response = getSeatsBySectionService.execute("E");

                assertThat(response.seats().get(0)).isFalse();  // E1
                assertThat(response.seats().get(23)).isFalse(); // E24
                assertThat(response.seats().get(24)).isTrue();  // E25
                assertThat(response.seats().get(71)).isTrue();  // E72
                assertThat(response.seats().get(72)).isFalse(); // E73
                assertThat(response.seats().get(95)).isFalse(); // E96
            }
        }
    }

    @Test
    void B와_C구역의_공연자_좌석_경계를_반영한다() {
        given(seatReservationCustomRepository.findSeatNumbersBySeatSection(anyString())).willReturn(Set.of());
        given(seatBanCustomRepository.findSeatNumbersBySeatSection(anyString())).willReturn(Set.of());

        try (MockedStatic<UserUtil> userUtilMock = mockStatic(UserUtil.class)) {
            userUtilMock.when(UserUtil::getCurrentUserRole).thenReturn(Role.PERFORMER);

            GetSeatsBySectionResponse b = getSeatsBySectionService.execute("B");
            assertThat(b.seats().get(11)).isFalse(); // B12
            assertThat(b.seats().get(12)).isTrue();  // B13
            assertThat(b.seats().get(38)).isTrue();  // B39
            assertThat(b.seats().get(39)).isFalse(); // B40
            assertThat(b.seats().get(42)).isFalse(); // B43
            assertThat(b.seats().get(43)).isTrue();  // B44
            assertThat(b.seats().get(47)).isTrue();  // B48
            assertThat(b.seats().get(48)).isFalse(); // B49

            GetSeatsBySectionResponse c = getSeatsBySectionService.execute("C");
            assertThat(c.seats().get(6)).isFalse();  // C7
            assertThat(c.seats().get(7)).isTrue();   // C8
            assertThat(c.seats().get(12)).isTrue();  // C13
            assertThat(c.seats().get(13)).isFalse(); // C14
            assertThat(c.seats().get(14)).isFalse(); // C15
            assertThat(c.seats().get(15)).isTrue();  // C16
            assertThat(c.seats().get(31)).isTrue();  // C32
            assertThat(c.seats().get(32)).isFalse(); // C33
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
