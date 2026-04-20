package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

@Component
@RequiredArgsConstructor
public class SeatReservationValidator {

    private final SeatReservationRepository seatReservationRepository;
    private final UserUtil userUtil;

    private static final int PERFORMER_SEAT_LIMIT = 3;
    private static final int DEFAULT_SEAT_LIMIT = 1;
    private static final int RESERVED = 1;
    private static final int BANNED = 2;

    public void validateSeatRange(Integer seatNumber, Integer maxSeats) {
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }
    }

    public void validateSeatAvailability(String seatSection, Integer seatNumber) {
        int availability = seatReservationRepository.checkAvailability(seatSection, seatNumber);
        if ((availability & RESERVED) != 0) throw new SeatAlreadyReservedException();
        if ((availability & BANNED) != 0) throw new SeatBannedException();
    }

    public void validateReservationLimit() {
        long userId = userUtil.getCurrentUserId();
        long reserveCount = seatReservationRepository.countByUserId(userId);
        int limit = userUtil.currentUserRole() == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;
        if (reserveCount >= limit) {
            throw new SeatReservationLimitExceededException();
        }
    }
}
