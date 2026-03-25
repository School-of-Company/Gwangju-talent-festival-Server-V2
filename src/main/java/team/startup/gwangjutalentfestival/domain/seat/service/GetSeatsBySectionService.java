package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatsBySectionResponse;

public interface GetSeatsBySectionService {
    GetSeatsBySectionResponse execute(String section);
}
