package team.startup.gwangjutalentfestival.global.thirdparty.google.exception;

import org.junit.jupiter.api.Test;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsConfigMissingException.require;

class GoogleSheetsConfigMissingExceptionTest {

    @Test
    void 설정값이_있으면_그대로_반환한다() {
        assertThat(require("sheet-id", "google.excel.template-sheet-id")).isEqualTo("sheet-id");
    }

    @Test
    void 설정값이_null이면_GoogleSheetsConfigMissingException이_발생한다() {
        assertThatThrownBy(() -> require(null, "google.excel.template-sheet-id"))
                .isInstanceOf(GoogleSheetsConfigMissingException.class)
                .extracting(e -> ((GoogleSheetsConfigMissingException) e).getErrorCode())
                .isEqualTo(ErrorCode.GOOGLE_CONFIG_MISSING);
    }

    @Test
    void 설정값이_공백이면_GoogleSheetsConfigMissingException이_발생한다() {
        assertThatThrownBy(() -> require(" ", "google.sheets.sheet-id"))
                .isInstanceOf(GoogleSheetsConfigMissingException.class);
    }
}
