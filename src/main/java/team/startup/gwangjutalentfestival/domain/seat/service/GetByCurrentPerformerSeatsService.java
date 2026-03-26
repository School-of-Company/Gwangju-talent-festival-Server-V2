package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.response.GetSeatResponse;

import java.util.List;

public interface GetByCurrentPerformerSeatsService {
    List<GetSeatResponse> execute();
}
