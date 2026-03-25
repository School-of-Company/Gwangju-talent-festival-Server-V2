package team.startup.gwangjutalentfestival.domain.seat.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;

@Service
@RequiredArgsConstructor
public class BanSeatServiceImpl implements BanSeatService {

    private final SeatBanRepository seatBanRepository;

    @Override
    @Transactional
    public void execute(BanSeatRequest request) {
        Character seatSection = request.seatSection().charAt(0);

        if (seatBanRepository
                .existsBySeatSectionAndSeatNumber(seatSection, request.seatNumber())) {
            throw new SeatAlreadyReservedException();
        }

        SeatBanEntity seatBan = SeatBanEntity.builder()
                .seatSection(seatSection)
                .seatNumber(request.seatNumber())
                .role(request.role())
                .build();

        seatBanRepository.save(seatBan);
    }
}
