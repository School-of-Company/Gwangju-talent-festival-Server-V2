package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 인증번호가 만료되어 Redis에서 조회되지 않을 때 발생하는 예외.
 */
public class ExpiredVerifyCodeException extends GlobalException {
    public ExpiredVerifyCodeException() {
        super(ErrorCode.EXPIRED_VERIFY_CODE);
    }
}
