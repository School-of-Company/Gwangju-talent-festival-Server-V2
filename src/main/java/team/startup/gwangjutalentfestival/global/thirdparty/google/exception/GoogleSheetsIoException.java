package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Google Sheets 서버와 통신 중 IO 오류가 발생했을 때 던지는 예외.
 * <p>네트워크 장애 등 {@link java.io.IOException} 발생 시 변환된다.</p>
 */
public class GoogleSheetsIoException extends GlobalException {
    public GoogleSheetsIoException() {
        super(ErrorCode.GOOGLE_SHEETS_IO_ERROR);
    }
}
