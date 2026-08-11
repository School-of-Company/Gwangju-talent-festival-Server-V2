package team.startup.gwangjutalentfestival.global.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeatUtilTest {

    private final SeatUtil seatUtil = new SeatUtil();

    @Test
    void 장애인석_W구역은_제공하지_않는다() {
        assertThat(seatUtil.getSections()).containsExactly("A", "B", "C", "D", "E", "F");
    }

    @ParameterizedTest
    @CsvSource({
            "A, 15, false", "A, 16, true", "A, 32, true", "A, 33, false",
            "B, 12, false", "B, 13, true", "B, 39, true", "B, 40, false",
            "B, 43, false", "B, 44, true", "B, 48, true", "B, 49, false",
            "C, 7, false", "C, 8, true", "C, 13, true", "C, 14, false",
            "C, 15, false", "C, 16, true", "C, 32, true", "C, 33, false",
            "D, 1, false", "E, 1, false", "F, 1, false"
    })
    void 공연자_좌석_범위를_검증한다(String section, int seatNumber, boolean performerAllowed) {
        assertThat(seatUtil.isAllowedForRole(Role.PERFORMER, section, seatNumber)).isEqualTo(performerAllowed);
    }

    @Test
    void 역할별_좌석과_정적_금지_좌석을_전체_좌석에서_분리한다() {
        Set<String> expectedPerformerSeats = Set.of(
                range("A", 16, 32),
                range("B", 13, 39),
                range("B", 44, 48),
                range("C", 8, 13),
                range("C", 16, 32)
        ).stream().flatMap(List::stream).collect(java.util.stream.Collectors.toSet());
        Set<String> restrictedSeats = Set.of(
                range("A", 1, 15),
                range("B", 1, 12),
                range("C", 1, 7),
                range("C", 14, 15),
                range("E", 1, 24),
                range("E", 73, 96)
        ).stream().flatMap(List::stream).collect(java.util.stream.Collectors.toSet());

        List<String> allSeats = seatUtil.getSections().stream()
                .flatMap(section -> range(section, 1, seatUtil.getMaxSeats(section)).stream())
                .toList();
        List<String> performerSeats = allSeats.stream()
                .filter(seat -> allowed(Role.PERFORMER, seat))
                .toList();
        List<String> userSeats = allSeats.stream()
                .filter(seat -> allowed(Role.USER, seat))
                .toList();
        List<String> expectedUserSeats = allSeats.stream()
                .filter(seat -> !expectedPerformerSeats.contains(seat) && !restrictedSeats.contains(seat))
                .toList();

        assertThat(performerSeats).containsExactlyInAnyOrderElementsOf(expectedPerformerSeats);
        assertThat(performerSeats).hasSize(72);
        assertThat(restrictedSeats).hasSize(84);
        assertThat(userSeats).containsExactlyInAnyOrderElementsOf(expectedUserSeats);
        assertThat(userSeats).hasSize(452);
        assertThat(performerSeats).doesNotContainAnyElementsOf(userSeats);
        assertThat(performerSeats).hasSize(allSeats.size() - userSeats.size() - restrictedSeats.size());
        for (Role role : Role.values()) {
            assertThat(restrictedSeats).allSatisfy(seat -> assertThat(allowed(role, seat)).isFalse());
        }
    }

    private List<String> range(String section, int start, int end) {
        return IntStream.rangeClosed(start, end)
                .mapToObj(number -> section + ":" + number)
                .toList();
    }

    private boolean allowed(Role role, String seat) {
        String[] parts = seat.split(":");
        return seatUtil.isAllowedForRole(role, parts[0], Integer.parseInt(parts[1]));
    }
}
