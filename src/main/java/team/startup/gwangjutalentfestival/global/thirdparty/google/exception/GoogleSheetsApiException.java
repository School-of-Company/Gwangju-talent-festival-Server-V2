package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class GoogleSheetsApiException extends GlobalException {
    public GoogleSheetsApiException() {
        super(ErrorCode.GOOGLE_SHEETS_API_ERROR);
    }
}
