package team.startup.gwangjutalentfestival.global.oauth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class OAuthProviderNotSupportedException extends GlobalException {
    public OAuthProviderNotSupportedException() {
        super(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
    }
}
