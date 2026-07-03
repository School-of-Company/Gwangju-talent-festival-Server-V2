package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * RefreshToken이 유효하지 않거나 저장된 토큰과 일치하지 않을 때 발생하는 예외.
 */
public class InvalidRefreshTokenException extends GlobalException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
