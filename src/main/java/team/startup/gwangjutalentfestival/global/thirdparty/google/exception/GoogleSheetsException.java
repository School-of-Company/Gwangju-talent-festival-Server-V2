package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class GoogleSheetsException extends GlobalException {
    public GoogleSheetsException() {
        super(ErrorCode.GOOGLE_SHEETS);
    }
}
