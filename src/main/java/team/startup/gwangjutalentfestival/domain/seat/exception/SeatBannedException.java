package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatBannedException extends GlobalException {
    public SeatBannedException() {
        super(ErrorCode.SEAT_BANNED);
    }
}
