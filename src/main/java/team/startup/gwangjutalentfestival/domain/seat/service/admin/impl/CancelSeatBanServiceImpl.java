package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBanNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;

@Service
@RequiredArgsConstructor
public class CancelSeatBanServiceImpl implements CancelSeatBanService {

    private final SeatBanRepository seatBanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void execute(CancelSeatBanRequest request) {
        Character seatSection = request.seatSection().charAt(0);

        SeatBanEntity seatBan = seatBanRepository
                .findBySeatSectionAndSeatNumber(seatSection, request.seatNumber())
                .orElseThrow(SeatBanNotFoundException::new);

        seatBanRepository.delete(seatBan);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                true
        ));
    }
}