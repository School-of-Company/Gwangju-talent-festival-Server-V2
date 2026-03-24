package team.startup.gwangjutalentfestival.global.util;

import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.InvalidSeatSectionException;

import java.util.Map;

@Component
public class SeatUtil {
    private static final Map<Character, Integer> SEAT_MAP = Map.of(
            'A', 77, 'B', 130, 'C', 154, 'D', 130, 'E', 77,
            'F', 54, 'G', 100, 'H', 119, 'I', 100, 'J', 54
    );

    public Integer getMaxSeats(Character section) {
        if (!SEAT_MAP.containsKey(section)) {
            throw new InvalidSeatSectionException();
        }
        return SEAT_MAP.get(section);
    }
}
