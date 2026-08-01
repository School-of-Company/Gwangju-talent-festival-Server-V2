package team.startup.gwangjutalentfestival.global.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

class SeatUtilTest {

    private final SeatUtil seatUtil = new SeatUtil();

    @Test
    void 장애인석_W구역은_제공하지_않는다() {
        assertThat(seatUtil.getSections()).containsExactly("A", "B", "C", "D", "E", "F");
    }

    @ParameterizedTest
    @CsvSource({
            "A, 1, true", "A, 15, true", "A, 16, false", "A, 20, false",
            "A, 21, true", "A, 23, true", "A, 24, false",
            "B, 1, true", "B, 36, true", "B, 37, false",
            "C, 1, true", "C, 18, true", "C, 19, false",
            "D, 1, false", "E, 1, false", "F, 1, false"
    })
    void 공연자_좌석_범위를_검증한다(String section, int seatNumber, boolean performerAllowed) {
        assertThat(seatUtil.isAllowedForRole(Role.PERFORMER, section, seatNumber)).isEqualTo(performerAllowed);
        assertThat(seatUtil.isAllowedForRole(Role.USER, section, seatNumber)).isEqualTo(!performerAllowed);
    }
}
