package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 이미 다른 사용자가 예약한 좌석을 예약하려 할 때 발생하는 예외.
 */
public class SeatAlreadyReservedException extends GlobalException {
    public SeatAlreadyReservedException() {
        super(ErrorCode.SEAT_ALREADY_RESERVED);
    }
}
