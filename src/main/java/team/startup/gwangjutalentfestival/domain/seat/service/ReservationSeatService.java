package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;

public interface ReservationSeatService {
    void execute(ReservationSeatRequest request);
}
