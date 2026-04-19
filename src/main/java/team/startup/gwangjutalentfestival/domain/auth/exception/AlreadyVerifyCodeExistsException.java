package team.startup.gwangjutalentfestival.domain.auth.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 동일한 휴대폰 번호로 인증번호가 이미 발급되어 있거나,
 * 허용된 최대 발송 횟수를 초과했을 때 발생하는 예외.
 */
public class AlreadyVerifyCodeExistsException extends GlobalException {
    public AlreadyVerifyCodeExistsException() {
        super(ErrorCode.ALREADY_VERIFY_CODE_EXISTS);
    }
}