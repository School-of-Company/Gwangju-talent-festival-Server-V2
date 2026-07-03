package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

@Component
@RequiredArgsConstructor
public class SeatReservationValidator {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatBanRepository seatBanRepository;
    private final UserRepository userRepository;

    private static final int PERFORMER_SEAT_LIMIT = 3;
    private static final int DEFAULT_SEAT_LIMIT = 1;

    public void validateSeatRange(Integer seatNumber, Integer maxSeats) {
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }
    }

    public void validateSeatAvailability(String seatSection, Integer seatNumber) {
        if (seatReservationRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) throw new SeatAlreadyReservedException();
        if (seatBanRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) throw new SeatBannedException();
    }

    public void validateReservationLimit() {
        long userId = UserUtil.getCurrentUserId();
        userRepository.findByIdForUpdate(userId);
        int limit = UserUtil.getCurrentUserRole() == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;
        if (seatReservationRepository.countByUserId(userId) >= limit) {
            throw new SeatReservationLimitExceededException();
        }
    }
}
