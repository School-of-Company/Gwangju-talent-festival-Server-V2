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
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

@Service
@RequiredArgsConstructor
public class ReservationSeatServiceImpl implements ReservationSeatService {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatUtil seatUtil;
    private final UserUtil userUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int PERFORMER_SEAT_LIMIT = 3;
    private static final int DEFAULT_SEAT_LIMIT = 1;
    private static final int RESERVED = 1;
    private static final int BANNED = 2;

    @Override
    @Transactional
    public void execute(ReservationSeatRequest request) {
        String seatSection = request.seatSection();
        Integer seatNumber = request.seatNumber();

        Integer maxSeats = seatUtil.getMaxSeats(seatSection);
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }

        int availability = seatReservationRepository.checkAvailability(seatSection, seatNumber);
        if ((availability & RESERVED) != 0) throw new SeatAlreadyReservedException();
        if ((availability & BANNED) != 0) throw new SeatBannedException();

        long userId = UserUtil.getCurrentUserId();
        long reserveCount = seatReservationRepository.countByUserId(userId);

        int limit = UserUtil.getCurrentUserRole() == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;

        if (reserveCount >= limit) {
            throw new SeatReservationLimitExceededException();
        }

        SeatEntity seat = SeatEntity.builder()
                .seatNumber(seatNumber)
                .seatSection(seatSection)
                .user(userUtil.getCurrentUserRef())
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
