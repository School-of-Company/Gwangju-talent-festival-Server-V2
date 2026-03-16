package team.startup.gwangjutalentfestival.global.sms.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SmsEmptyResponseException extends GlobalException {
    public SmsEmptyResponseException() {
        super(ErrorCode.SMS_EMPTY_RESPONSE);
    }
}
