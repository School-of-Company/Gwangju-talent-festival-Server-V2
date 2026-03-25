package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class CancelSeatReservationServiceImpl implements CancelSeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void execute() {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity seat = seatReservationRepository.findByUser(user)
                .orElseThrow(SeatNotFoundException::new);

        seatReservationRepository.delete(seat);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                seat.getSeatSection().toString(),
                seat.getSeatNumber(),
                true
        ));
    }
}