package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.PerformerCancelSeatReservationRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.PerformerCancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

@Service
@RequiredArgsConstructor
public class PerformerCancelSeatReservationServiceImpl implements PerformerCancelSeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void execute(PerformerCancelSeatReservationRequest request) {
        UserEntity user = userUtil.getCurrentUser();

        SeatEntity seat = seatReservationRepository.findBySeatSectionAndSeatNumberAndUser(
                request.seatSection().charAt(0),
                request.seatNumber(),
                user
        ).orElseThrow(SeatNotFoundException::new);

        seatReservationRepository.delete(seat);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                true
        ));
    }
}