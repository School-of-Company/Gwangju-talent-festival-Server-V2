package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

import java.util.List;

public record GetSeatsBySectionResponse(
        List<Boolean> seats
) {
}
