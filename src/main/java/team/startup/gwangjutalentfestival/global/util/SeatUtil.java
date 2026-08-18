package team.startup.gwangjutalentfestival.global.util;

import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.InvalidSeatSectionException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * 좌석 구역 및 최대 좌석 수 관리 유틸리티.
 * <p>구역별 최대 좌석 수를 내부 맵으로 관리하며, 유효하지 않은 구역 조회 시 예외를 던진다.</p>
 */
@Component
public class SeatUtil {
    public static final int MAX_SEAT_NUMBER = 132;

    private static final Map<String, Integer> SEAT_MAP = Map.of(
            "A", 101, "B", 132, "C", 101, "D", 89, "E", 96,
            "F", 89
    );

    /**
     * 전체 좌석 구역 목록을 알파벳 오름차순으로 반환한다.
     *
     * @return 정렬된 구역 문자열 목록
     */
    public List<String> getSections() {
        return SEAT_MAP.keySet().stream().sorted().toList();
    }

    /**
     * 지정된 구역의 최대 좌석 수를 반환한다.
     *
     * @param section 구역 문자 (예: "A", "B")
     * @return 해당 구역의 최대 좌석 수
     * @throws team.startup.gwangjutalentfestival.domain.seat.exception.InvalidSeatSectionException 유효하지 않은 구역인 경우
     */
    public Integer getMaxSeats(String section) {
        if (!SEAT_MAP.containsKey(section)) {
            throw new InvalidSeatSectionException();
        }
        return SEAT_MAP.get(section);
    }

    public boolean isAllowedForRole(Role role, String section, int seatNumber) {
        boolean restrictedSeat = switch (section) {
            case "A" -> seatNumber >= 1 && seatNumber <= 15;
            case "B" -> seatNumber >= 1 && seatNumber <= 12;
            case "C" -> seatNumber >= 1 && seatNumber <= 7
                    || seatNumber >= 14 && seatNumber <= 15;
            case "D" -> seatNumber >= 70 && seatNumber <= 75
                    || seatNumber >= 80 && seatNumber <= 89;
            case "E" -> seatNumber >= 1 && seatNumber <= 24
                    || seatNumber >= 73 && seatNumber <= 96;
            default -> false;
        };
        if (restrictedSeat) return false;

        boolean performerSeat = switch (section) {
            case "A" -> seatNumber >= 16 && seatNumber <= 32;
            case "B" -> seatNumber >= 13 && seatNumber <= 39
                    || seatNumber >= 44 && seatNumber <= 48;
            case "C" -> seatNumber >= 8 && seatNumber <= 13
                    || seatNumber >= 16 && seatNumber <= 32;
            default -> false;
        };

        return role == Role.PERFORMER ? performerSeat : role != Role.USER || !performerSeat;
    }

    public GetSeatsBySectionResponse buildSeatAvailability(
            String section,
            Set<Integer> bans,
            Set<Integer> reservations,
            Role role
    ) {
        List<Boolean> seats = IntStream.rangeClosed(1, getMaxSeats(section))
                .mapToObj(i -> isAllowedForRole(role, section, i) && !bans.contains(i) && !reservations.contains(i))
                .toList();
        return new GetSeatsBySectionResponse(seats);
    }
}
