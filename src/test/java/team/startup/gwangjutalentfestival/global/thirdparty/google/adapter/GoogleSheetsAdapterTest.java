package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
