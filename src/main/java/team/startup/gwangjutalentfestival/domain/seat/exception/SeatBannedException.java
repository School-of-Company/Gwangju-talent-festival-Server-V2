package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 관리자가 차단한 좌석을 예약하려 할 때 발생하는 예외.
 */
public class SeatBannedException extends GlobalException {
    public SeatBannedException() {
        super(ErrorCode.SEAT_BANNED);
    }
}
