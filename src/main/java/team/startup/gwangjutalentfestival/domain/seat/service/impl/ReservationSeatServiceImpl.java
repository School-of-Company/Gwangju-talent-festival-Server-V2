package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.event.SeatChangeEvent;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatBanRepository seatBanRepository;
    private final SeatUtil seatUtil;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int PERFORMER_SEAT_LIMIT = 3;
    private static final int DEFAULT_SEAT_LIMIT = 1;

    @Override
    @Transactional
    public void execute(ReservationSeatRequest request) {
        Character seatSection = request.seatSection().charAt(0);
        Integer seatNumber = request.seatNumber();

        Integer maxSeats = seatUtil.getMaxSeats(seatSection);
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }

        if (seatReservationRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) {
            throw new SeatAlreadyReservedException();
        }

        if (seatBanRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) {
            throw new SeatBannedException();
        }

        UserEntity user = userUtil.getCurrentUser();

        long reserveCount = seatReservationRepository.countByUser(user);

        int limit = user.getRole() == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;

        if (reserveCount >= limit) {
            throw new SeatReservationLimitExceededException();
        }

        SeatEntity seat = SeatEntity.builder()
                .seatNumber(seatNumber)
                .seatSection(seatSection)
                .user(user)
                .build();

        try {
            seatReservationRepository.saveAndFlush(seat);
        } catch (DataIntegrityViolationException e) {
            throw new SeatAlreadyReservedException();
        }

        applicationEventPublisher.publishEvent(new SeatChangeEvent(
                request.seatSection(),
                request.seatNumber(),
                false
        ));
    }
}