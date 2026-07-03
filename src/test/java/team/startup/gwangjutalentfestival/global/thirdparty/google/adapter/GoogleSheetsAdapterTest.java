package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSheetsAdapterTest {

    @Test
    void sheetPageWithSpacesIsConvertedToQuotedA1Range() {
        String range = GoogleSheetsAdapter.toAppendRange("학교 밖 청소년");

        assertThat(range).isEqualTo("'학교 밖 청소년'!A1");
    }

    @Test
    void sheetPageWithSingleQuoteEscapesQuote() {
        String range = GoogleSheetsAdapter.toAppendRange("학생's");

        assertThat(range).isEqualTo("'학생''s'!A1");
    }

    @Test
    void nullSheetPageThrowsException() {
        assertThatThrownBy(() -> GoogleSheetsAdapter.toAppendRange(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sheetPage must not be null or blank");
    }

    @Test
    void blankSheetPageThrowsException() {
        assertThatThrownBy(() -> GoogleSheetsAdapter.toAppendRange(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sheetPage must not be null or blank");
    }
}
