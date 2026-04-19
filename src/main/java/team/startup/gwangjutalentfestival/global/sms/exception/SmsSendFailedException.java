package team.startup.gwangjutalentfestival.global.sms.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * SMS 전송에 실패했을 때 발생하는 예외.
 * <p>Solapi API 호출 중 전송 오류 또는 알 수 없는 오류가 발생한 경우 발생한다.</p>
 */
public class SmsSendFailedException extends GlobalException {
    public SmsSendFailedException() {
        super(ErrorCode.SMS_SEND_FAILED);
    }
}
