package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class AlreadyVerifyCodeExistsException extends GlobalException {
    public AlreadyVerifyCodeExistsException() {
        super(ErrorCode.ALREADY_VERIFY_CODE_EXISTS);
    }
}