package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExceededVerifyCountException extends GlobalException {
    public ExceededVerifyCountException() {
        super(ErrorCode.EXCEEDED_VERIFY_COUNT);
    }
}
