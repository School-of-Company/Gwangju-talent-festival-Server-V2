package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 이미 차단 처리된 좌석을 다시 차단하려 할 때 발생하는 예외.
 */
public class SeatAlreadyBannedException extends GlobalException {
    public SeatAlreadyBannedException() {
        super(ErrorCode.SEAT_ALREADY_BANNED);
    }
}
