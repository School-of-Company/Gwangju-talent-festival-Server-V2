package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Google Sheets 클라이언트 초기화에 실패했을 때 던지는 예외.
 * <p>인증 파일 읽기 실패 또는 TLS 초기화 실패 시 발생한다.</p>
 */
public class GoogleSheetsInitException extends GlobalException {
    public GoogleSheetsInitException() {
        super(ErrorCode.GOOGLE_SHEETS_INIT_ERROR);
    }
}
