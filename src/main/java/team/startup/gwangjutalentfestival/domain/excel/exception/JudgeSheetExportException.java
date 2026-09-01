package team.startup.gwangjutalentfestival.domain.excel.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 심사표 파일(ZIP, 개별 xlsx) 생성에 실패했을 때 던지는 예외.
 */
public class JudgeSheetExportException extends GlobalException {
    public JudgeSheetExportException() {
        super(ErrorCode.JUDGE_SHEET_EXPORT_FAILED);
    }
}
