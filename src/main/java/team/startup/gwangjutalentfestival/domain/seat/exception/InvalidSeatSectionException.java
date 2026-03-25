package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidSeatSectionException extends GlobalException {
    public InvalidSeatSectionException() {
        super(ErrorCode.INVALID_SEAT_SECTION);
    }
}
