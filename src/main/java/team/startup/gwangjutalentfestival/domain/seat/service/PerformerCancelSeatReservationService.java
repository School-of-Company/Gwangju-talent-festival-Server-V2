package team.startup.gwangjutalentfestival.domain.seat.service;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;

public interface PerformerCancelSeatReservationService {
    void execute(PerformerCancelSeatReservationRequest request);
}
