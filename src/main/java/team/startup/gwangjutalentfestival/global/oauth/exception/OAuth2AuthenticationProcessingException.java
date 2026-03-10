package team.startup.gwangjutalentfestival.global.oauth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class OAuth2AuthenticationProcessingException extends GlobalException {
    public OAuth2AuthenticationProcessingException() {
        super(ErrorCode.OAUTH_PROCESSING_ERROR);
    }
}
