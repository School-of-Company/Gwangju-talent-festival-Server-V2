package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 동일한 좌석이 한 예약 요청에 중복으로 포함된 경우 발생하는 예외.
 */
public class DuplicateSeatRequestException extends GlobalException {

    public DuplicateSeatRequestException() {
        super(ErrorCode.DUPLICATE_SEAT_REQUEST);
    }
}
