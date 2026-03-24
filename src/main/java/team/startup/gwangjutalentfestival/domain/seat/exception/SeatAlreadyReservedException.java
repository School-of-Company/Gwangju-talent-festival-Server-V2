package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatAlreadyReservedException extends GlobalException {
    public SeatAlreadyReservedException() {
        super(ErrorCode.SEAT_ALREADY_RESERVED);
    }
}
