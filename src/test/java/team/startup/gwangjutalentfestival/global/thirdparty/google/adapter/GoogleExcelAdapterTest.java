package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleExcelProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleExcelAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Sheets sheets;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Drive drive;

    @Mock
    private GoogleExcelProperties properties;

    private GoogleExcelAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoogleExcelAdapter(sheets, drive, properties);
    }

    private void stubProperties() {
        given(properties.templateSheetId()).willReturn("SHEET_ID");
        given(properties.summaryPage()).willReturn("심사집계표");
    }

    private void stubGridSheetId(int gridSheetId) throws Exception {
        SheetProperties sheetProperties = new SheetProperties().setTitle("심사집계표").setSheetId(gridSheetId);
        Spreadsheet spreadsheet = new Spreadsheet().setSheets(List.of(new Sheet().setProperties(sheetProperties)));
        given(sheets.spreadsheets().get("SHEET_ID").setFields(anyString()).execute()).willReturn(spreadsheet);
    }

    @Test
    void 정상_흐름에서_clear_서식적용_write_export가_수행된다() throws Exception {
        stubProperties();
        stubGridSheetId(42);
        List<List<Object>> rows = List.of(
                List.of("심사번호", "심사위원 (A)", "산출점수", "순위"),
                List.of(1, 80, 80, 1)
        );

        byte[] result = adapter.exportSummary(rows);

        assertThat(result).isNotNull();
        verify(sheets.spreadsheets().values(), times(2))
                .clear(eq("SHEET_ID"), eq("'심사집계표'!A3:D1000"), any());
        verify(sheets.spreadsheets().values())
                .update(eq("SHEET_ID"), eq("'심사집계표'!A3"), any());
        verify(sheets.spreadsheets())
                .batchUpdate(eq("SHEET_ID"), any(BatchUpdateSpreadsheetRequest.class));
        verify(drive.files().export(eq("SHEET_ID"), anyString()))
                .executeMediaAndDownloadTo(any());
    }

    @Test
    void 데이터가_없으면_서식_배치업데이트_없이_진행된다() throws Exception {
        stubProperties();
        byte[] result = adapter.exportSummary(List.of());

        assertThat(result).isNotNull();
        verify(sheets.spreadsheets(), never()).batchUpdate(anyString(), any());
    }

    @Test
    void 템플릿에_해당_페이지_시트가_없으면_GoogleSheetsException이_발생한다() throws Exception {
        stubProperties();
        Spreadsheet spreadsheet = new Spreadsheet().setSheets(List.of());
        given(sheets.spreadsheets().get("SHEET_ID").setFields(anyString()).execute()).willReturn(spreadsheet);

        List<List<Object>> rows = List.of(List.of("심사번호", "산출점수", "순위"));

        assertThatThrownBy(() -> adapter.exportSummary(rows))
                .isInstanceOf(GoogleSheetsException.class);
    }

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

    @Test
    void 컬럼_26개는_Z로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(26)).isEqualTo("Z");
    }

    @Test
    void 컬럼_27개는_AA로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(27)).isEqualTo("AA");
    }

    @Test
    void 컬럼_52개는_AZ로_변환된다() {
        assertThat(GoogleExcelAdapter.columnLetter(52)).isEqualTo("AZ");
    }
}
