package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 관리자가 차단했거나 현재 역할에 허용되지 않은 좌석을 예약할 때 발생하는 예외.
 */
public class SeatBannedException extends GlobalException {
    public SeatBannedException() {
        super(ErrorCode.SEAT_BANNED);
    }
}
