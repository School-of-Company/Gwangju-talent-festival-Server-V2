package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Google Sheets API 호출 중 API 오류가 발생했을 때 던지는 예외.
 * <p>{@link com.google.api.client.googleapis.json.GoogleJsonResponseException} 발생 시 변환된다.</p>
 */
public class GoogleSheetsApiException extends GlobalException {
    public GoogleSheetsApiException() {
        super(ErrorCode.GOOGLE_SHEETS_API_ERROR);
    }
}
