package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SeatNotExistsInSectionException extends GlobalException {
    public SeatNotExistsInSectionException() {
        super(ErrorCode.SEAT_NOT_EXISTS_IN_SECTION);
    }
}