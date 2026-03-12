package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExpiredVerifyCodeException extends GlobalException {
    public ExpiredVerifyCodeException() {
        super(ErrorCode.EXPIRED_VERIFY_CODE);
    }
}
