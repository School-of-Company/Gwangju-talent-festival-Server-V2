package team.startup.gwangjutalentfestival.domain.seat.presentation.data.response;

import java.util.Map;

public record GetAllSeatsResponse(
    Map<String, GetSeatsBySectionResponse> sections
) {
}