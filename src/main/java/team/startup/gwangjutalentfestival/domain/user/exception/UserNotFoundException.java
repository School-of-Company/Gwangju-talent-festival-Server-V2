package team.startup.gwangjutalentfestival.domain.user.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 요청한 사용자가 존재하지 않을 때 발생하는 예외.
 */
public class UserNotFoundException extends GlobalException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
