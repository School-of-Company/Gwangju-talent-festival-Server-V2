package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import lombok.extern.slf4j.Slf4j;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * Google 연동에 필요한 설정값이 비어 있을 때 던지는 예외.
 * <p>빈 스프레드시트 ID로 구글을 호출해 원인을 알기 어려운 400을 받는 대신,
 * 호출 전에 어떤 프로퍼티가 누락됐는지 로그로 남기고 중단한다.</p>
 */
@Slf4j
public class GoogleSheetsConfigMissingException extends GlobalException {
    public GoogleSheetsConfigMissingException() {
        super(ErrorCode.GOOGLE_CONFIG_MISSING);
    }

    /**
     * 설정값이 비어 있으면 누락된 프로퍼티 키를 로그로 남기고 중단한다.
     *
     * @param value    검사할 설정값
     * @param property 로그에 남길 프로퍼티 키 (예: {@code google.excel.template-sheet-id})
     * @return 비어 있지 않은 설정값
     */
    public static String require(String value, String property) {
        if (value == null || value.isBlank()) {
            log.error("Google 연동 설정 누락 - property: {}", property);
            throw new GoogleSheetsConfigMissingException();
        }
        return value;
    }
}
