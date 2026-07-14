package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleExcelAdapterTest {

    @Test
    void 컬럼_1개는_A로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(1)).isEqualTo("A");
    }

    @Test
    void 컬럼_9개는_I로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(9)).isEqualTo("I");
    }

    @Test
    void 컬럼_0개는_A로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(0)).isEqualTo("A");
    }
}
