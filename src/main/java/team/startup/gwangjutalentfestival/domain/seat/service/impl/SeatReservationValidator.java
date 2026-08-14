package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotExistsInSectionException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationPeriodException;
import team.startup.gwangjutalentfestival.domain.seat.properties.SeatReservationPeriodProperties;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;
import team.startup.gwangjutalentfestival.global.util.SeatUtil;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.time.LocalDateTime;

import static team.startup.gwangjutalentfestival.domain.user.enums.Role.PERFORMER;

@Component
@RequiredArgsConstructor
public class SeatReservationValidator {

    private final SeatReservationRepository seatReservationRepository;
    private final SeatBanRepository seatBanRepository;
    private final UserRepository userRepository;
    private final SeatUtil seatUtil;
    private final SeatReservationPeriodProperties seatReservationPeriodProperties;

    private static final int PERFORMER_SEAT_LIMIT = 2;
    private static final int DEFAULT_SEAT_LIMIT = 1;

    public void validateReservationPeriod(Role role) {
        LocalDateTime now = LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID);
        if (!seatReservationPeriodProperties.of(role).contains(now)) {
            throw new SeatReservationPeriodException();
        }
    }

    public void validateSeatRange(Integer seatNumber, Integer maxSeats) {
        if (seatNumber < 1 || seatNumber > maxSeats) {
            throw new SeatNotExistsInSectionException();
        }
    }

    public void validateSeatAvailability(String seatSection, Integer seatNumber) {
        if (seatReservationRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) throw new SeatAlreadyReservedException();
        if (seatBanRepository.existsBySeatSectionAndSeatNumber(seatSection, seatNumber)) throw new SeatBannedException();
    }

    public void validateSeatAccess(String seatSection, Integer seatNumber) {
        validateSeatAccess(UserUtil.getCurrentUserRole(), seatSection, seatNumber);
    }

    public void validateSeatAccess(Role role, String seatSection, Integer seatNumber) {
        if (!seatUtil.isAllowedForRole(role, seatSection, seatNumber)) {
            throw new SeatBannedException();
        }
    }

    public UserEntity lockCurrentUser() {
        long userId = UserUtil.getCurrentUserId();
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public void validateReservationLimit(Role role, long currentReservationCount, int newReservationCount) {
        int limit = role == PERFORMER ? PERFORMER_SEAT_LIMIT : DEFAULT_SEAT_LIMIT;
        if (currentReservationCount + newReservationCount > limit) {
            throw new SeatReservationLimitExceededException();
        }
    }
}
