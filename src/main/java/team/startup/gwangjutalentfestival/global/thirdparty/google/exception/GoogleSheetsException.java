package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Google Sheets 연동 중 예상치 못한 오류가 발생했을 때 던지는 예외.
 */
public class GoogleSheetsException extends GlobalException {
    public GoogleSheetsException() {
        super(ErrorCode.GOOGLE_SHEETS);
    }
}
