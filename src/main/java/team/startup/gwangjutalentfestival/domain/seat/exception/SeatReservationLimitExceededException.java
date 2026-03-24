package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatReservationLimitExceededException extends GlobalException {
    public SeatReservationLimitExceededException() {
        super(ErrorCode.SEAT_RESERVATION_LIMIT_EXCEEDED);
    }
}
