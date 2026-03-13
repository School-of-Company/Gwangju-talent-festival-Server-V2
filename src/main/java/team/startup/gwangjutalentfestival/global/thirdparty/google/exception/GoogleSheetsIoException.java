package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class GoogleSheetsIoException extends GlobalException {
    public GoogleSheetsIoException() {
        super(ErrorCode.GOOGLE_SHEETS_IO_ERROR);
    }
}
