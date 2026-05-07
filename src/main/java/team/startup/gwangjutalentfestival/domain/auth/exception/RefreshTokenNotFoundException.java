package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Redis에서 해당 사용자의 RefreshToken을 찾을 수 없을 때 발생하는 예외.
 */
public class RefreshTokenNotFoundException extends GlobalException {
    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
