package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidVerifyCodeException extends GlobalException {
    public InvalidVerifyCodeException() {
        super(ErrorCode.INVALID_VERIFY_CODE);
    }
}
