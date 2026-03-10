package team.startup.gwangjutalentfestival.global.oauth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class UnsupportedOAuthProviderException extends GlobalException {
    public UnsupportedOAuthProviderException() {
        super(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
    }
}
