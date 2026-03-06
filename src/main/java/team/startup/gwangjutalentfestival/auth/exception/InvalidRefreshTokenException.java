package team.startup.gwangjutalentfestival.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidRefreshTokenException extends GlobalException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
