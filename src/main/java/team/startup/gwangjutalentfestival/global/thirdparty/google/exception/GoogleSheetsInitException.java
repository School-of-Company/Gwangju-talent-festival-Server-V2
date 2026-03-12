package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class GoogleSheetsInitException extends GlobalException {
    public GoogleSheetsInitException() {
        super(ErrorCode.GOOGLE_SHEETS_INIT_ERROR);
    }
}
