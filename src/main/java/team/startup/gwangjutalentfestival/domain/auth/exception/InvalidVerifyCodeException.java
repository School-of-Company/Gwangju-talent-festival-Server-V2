package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 입력한 인증번호가 발급된 인증번호와 일치하지 않을 때 발생하는 예외.
 */
public class InvalidVerifyCodeException extends GlobalException {
    public InvalidVerifyCodeException() {
        super(ErrorCode.INVALID_VERIFY_CODE);
    }
}
