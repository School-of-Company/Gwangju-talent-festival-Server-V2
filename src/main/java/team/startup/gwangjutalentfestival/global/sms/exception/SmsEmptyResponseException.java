package team.startup.gwangjutalentfestival.global.sms.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * SMS 전송 시 응답이 비어 있을 때 발생하는 예외.
 * <p>Solapi API가 빈 응답을 반환하는 경우 발생한다.</p>
 */
public class SmsEmptyResponseException extends GlobalException {
    public SmsEmptyResponseException() {
        super(ErrorCode.SMS_EMPTY_RESPONSE);
    }
}
