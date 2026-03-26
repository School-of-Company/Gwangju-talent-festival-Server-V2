package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;

@Service
@RequiredArgsConstructor
public class BanSeatServiceImpl implements BanSeatService {

    private final SeatBanRepository seatBanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void execute(BanSeatRequest request) {
        if (seatBanRepository
                .existsBySeatSectionAndSeatNumber(request.seatSection(), request.seatNumber())) {
            throw new SeatAlreadyReservedException();
        }

        SeatBanEntity seatBan = SeatBanEntity.builder()
                .seatSection(request.seatSection())
                .seatNumber(request.seatNumber())
                .role(request.role())
                .build();

        seatBanRepository.save(seatBan);

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                false
        ));
    }
}
