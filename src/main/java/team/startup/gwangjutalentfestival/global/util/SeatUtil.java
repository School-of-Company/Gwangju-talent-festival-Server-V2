package team.startup.gwangjutalentfestival.global.util;

import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.InvalidSeatSectionException;

import java.util.List;
import java.util.Map;

/**
 * 좌석 구역 및 최대 좌석 수 관리 유틸리티.
 * <p>구역별 최대 좌석 수를 내부 맵으로 관리하며, 유효하지 않은 구역 조회 시 예외를 던진다.</p>
 */
@Component
public class SeatUtil {
    private static final Map<String, Integer> SEAT_MAP = Map.of(
            "A", 77, "B", 130, "C", 154, "D", 130, "E", 77,
            "F", 54, "G", 100, "H", 119, "I", 100, "J", 54
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
}
