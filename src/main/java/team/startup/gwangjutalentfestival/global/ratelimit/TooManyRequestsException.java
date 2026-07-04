package team.startup.gwangjutalentfestival.global.ratelimit;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 쿨다운 시간 내에 동일 사용자가 재요청했을 때 발생하는 예외.
 */
public class TooManyRequestsException extends GlobalException {
    public TooManyRequestsException() {
        super(ErrorCode.TOO_MANY_REQUESTS);
    }
}
