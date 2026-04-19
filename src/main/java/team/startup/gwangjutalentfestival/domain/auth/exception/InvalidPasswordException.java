package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 입력한 비밀번호가 저장된 비밀번호와 일치하지 않을 때 발생하는 예외.
 */
public class InvalidPasswordException extends GlobalException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
