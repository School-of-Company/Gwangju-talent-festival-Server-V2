package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatAlreadyBannedException extends GlobalException {
    public SeatAlreadyBannedException() {
        super(ErrorCode.SEAT_ALREADY_BANNED);
    }
}
