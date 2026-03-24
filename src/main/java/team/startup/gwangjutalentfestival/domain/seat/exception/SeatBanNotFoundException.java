package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatBanNotFoundException extends GlobalException {
    public SeatBanNotFoundException() {
        super(ErrorCode.SEAT_BAN_NOT_FOUND);
    }
}
