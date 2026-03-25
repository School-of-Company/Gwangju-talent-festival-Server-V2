package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatNotFoundException extends GlobalException {
    public SeatNotFoundException() {
        super(ErrorCode.SEAT_NOT_FOUND);
    }
}
