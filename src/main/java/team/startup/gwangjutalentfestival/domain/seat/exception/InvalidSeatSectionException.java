package team.startup.gwangjutalentfestival.domain.seat.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 유효하지 않은 좌석 구역(A~J 범위 외)이 입력되었을 때 발생하는 예외.
 */
public class InvalidSeatSectionException extends GlobalException {
    public InvalidSeatSectionException() {
        super(ErrorCode.INVALID_SEAT_SECTION);
    }
}
