package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;

@RequiredArgsConstructor
public abstract class AbstractCancelSeatReservationService {

    protected final SeatReservationRepository seatReservationRepository;
    protected final ApplicationEventPublisher applicationEventPublisher;

    protected void cancelAndPublish(SeatEntity seat) {
        seatReservationRepository.delete(seat);
        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                seat.getSeatSection(),
                seat.getSeatNumber(),
                true
        ));
    }
}
