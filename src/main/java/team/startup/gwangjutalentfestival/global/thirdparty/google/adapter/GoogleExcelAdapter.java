package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Border;
import com.google.api.services.sheets.v4.model.Borders;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.DimensionProperties;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.MergeCellsRequest;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UnmergeCellsRequest;
import com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsApiException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsIoException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleExcelProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsConfigMissingException.require;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleExcelAdapter {

    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final int HEADER_ROW = 3;
    private static final int TITLE_ROWS = 2;
    // ponytail: 클리어 범위는 이전 실행의 잔여 데이터까지 지우도록 넉넉한 하한을 유지
    private static final int CLEAR_ROW_FLOOR = 1000;
    // ponytail: 제목/확인자 병합 해제 시 안전하게 잡는 넉넉한 컬럼 상한 (26컬럼 가정과 동일)
    private static final int MERGE_COLUMN_CEILING = 26;
    // 1~2행(제목/확인자)은 고정, 헤더 행부터 데이터 행까지만 이 크기로 맞춘다.
    private static final int COLUMN_WIDTH_PX = 100;
    private static final int ROW_HEIGHT_PX = 50;

    private final Sheets sheets;
    private final Drive drive;
    private final GoogleExcelProperties properties;

    public synchronized byte[] exportSummary(List<List<Object>> rows) {
        String sheetId = require(properties.templateSheetId(), "google.excel.template-sheet-id");
        String page = require(properties.summaryPage(), "google.excel.summary-page").replace("'", "''");
        int columnCount = rows.isEmpty() ? 0 : rows.get(0).size();
        String endColumn = columnLetter(columnCount);
        String writeRange = "'" + page + "'!A" + HEADER_ROW;
        String clearRange = "'" + page + "'!A" + HEADER_ROW + ":" + endColumn + CLEAR_ROW_FLOOR;
        int lastDataRow = HEADER_ROW + rows.size() - 1;

        try {
            sheets.spreadsheets().values()
                    .clear(sheetId, clearRange, new ClearValuesRequest())
                    .execute();

            formatTable(sheetId, page, columnCount, lastDataRow);

            ValueRange valueRange = new ValueRange().setValues(rows);
            sheets.spreadsheets().values()
                    .update(sheetId, writeRange, valueRange)
                    .setValueInputOption("RAW")
                    .execute();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            drive.files().export(sheetId, XLSX_MIME_TYPE)
                    .executeMediaAndDownloadTo(outputStream);

            return outputStream.toByteArray();

        } catch (GoogleJsonResponseException e) {
            log.error("Google Drive/Sheets API 오류 - statusCode: {}, message: {}", e.getStatusCode(), e.getMessage());
            throw new GoogleSheetsApiException();
        } catch (IOException e) {
            log.error("Google Excel export IO 오류 - message: {}", e.getMessage());
            throw new GoogleSheetsIoException();
        } catch (Exception e) {
            log.error("Google Excel export 예기치 못한 오류 - message: {}", e.getMessage());
            throw new GoogleSheetsException();
        } finally {
            try {
                sheets.spreadsheets().values()
                        .clear(sheetId, clearRange, new ClearValuesRequest())
                        .execute();
            } catch (Exception e) {
                // 사후 초기화 실패 시 다음 요청의 사전 초기화(pre-clear)에서 자동으로 처리됩니다.
                log.error("Google Sheets 사후 데이터 초기화 실패 - message: {}", e.getMessage());
            }
        }
    }

    public byte[] exportJudgeTemplate() {
        String judgeTemplateSheetId = require(properties.judgeTemplateSheetId(), "google.excel.judge-template-sheet-id");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            drive.files().export(judgeTemplateSheetId, XLSX_MIME_TYPE)
                    .executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        } catch (GoogleJsonResponseException e) {
            log.error("개별 심사표 템플릿 export API 오류 - statusCode: {}, message: {}", e.getStatusCode(), e.getMessage());
            throw new GoogleSheetsApiException();
        } catch (IOException e) {
            log.error("개별 심사표 템플릿 export IO 오류 - message: {}", e.getMessage());
            throw new GoogleSheetsIoException();
        } catch (Exception e) {
            log.error("개별 심사표 템플릿 export 예기치 못한 오류 - message: {}", e.getMessage());
            throw new GoogleSheetsException();
        }
    }

    private void formatTable(String spreadsheetId, String page, int columnCount, int lastDataRow) throws IOException {
        if (columnCount == 0) {
            return;
        }

        Integer gridSheetId = resolveGridSheetId(spreadsheetId, page);
        List<Request> requests = new ArrayList<>();

        requests.add(new Request().setUnmergeCells(new UnmergeCellsRequest()
                .setRange(gridRange(gridSheetId, 0, TITLE_ROWS, 0, MERGE_COLUMN_CEILING))));
        for (int titleRow = 0; titleRow < TITLE_ROWS; titleRow++) {
            requests.add(new Request().setMergeCells(new MergeCellsRequest()
                    .setRange(gridRange(gridSheetId, titleRow, titleRow + 1, 0, columnCount))
                    .setMergeType("MERGE_ALL")));
        }

        Border thinBorder = new Border().setStyle("SOLID").setColor(new Color().setRed(0f).setGreen(0f).setBlue(0f));
        Borders tableBorders = new Borders().setTop(thinBorder).setBottom(thinBorder).setLeft(thinBorder).setRight(thinBorder);
        CellFormat bodyFormat = new CellFormat()
                .setBorders(tableBorders)
                .setHorizontalAlignment("CENTER")
                .setVerticalAlignment("MIDDLE");
        requests.add(new Request().setRepeatCell(new RepeatCellRequest()
                .setRange(gridRange(gridSheetId, HEADER_ROW - 1, lastDataRow, 0, columnCount))
                .setCell(new CellData().setUserEnteredFormat(bodyFormat))
                .setFields("userEnteredFormat(borders,horizontalAlignment,verticalAlignment)")));

        CellFormat headerFormat = new CellFormat()
                .setBackgroundColor(new Color().setRed(0.9f).setGreen(0.9f).setBlue(0.93f))
                .setTextFormat(new TextFormat().setBold(true));
        requests.add(new Request().setRepeatCell(new RepeatCellRequest()
                .setRange(gridRange(gridSheetId, HEADER_ROW - 1, HEADER_ROW, 0, columnCount))
                .setCell(new CellData().setUserEnteredFormat(headerFormat))
                .setFields("userEnteredFormat(backgroundColor,textFormat.bold)")));

        requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
                .setRange(new DimensionRange().setSheetId(gridSheetId).setDimension("COLUMNS")
                        .setStartIndex(0).setEndIndex(columnCount))
                .setProperties(new DimensionProperties().setPixelSize(COLUMN_WIDTH_PX))
                .setFields("pixelSize")));
        requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
                .setRange(new DimensionRange().setSheetId(gridSheetId).setDimension("ROWS")
                        .setStartIndex(HEADER_ROW - 1).setEndIndex(lastDataRow))
                .setProperties(new DimensionProperties().setPixelSize(ROW_HEIGHT_PX))
                .setFields("pixelSize")));

        sheets.spreadsheets()
                .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests))
                .execute();
    }

    private GridRange gridRange(Integer gridSheetId, int startRow, int endRow, int startColumn, int endColumn) {
        return new GridRange()
                .setSheetId(gridSheetId)
                .setStartRowIndex(startRow)
                .setEndRowIndex(endRow)
                .setStartColumnIndex(startColumn)
                .setEndColumnIndex(endColumn);
    }

    private Integer resolveGridSheetId(String spreadsheetId, String page) throws IOException {
        String unescapedPage = page.replace("''", "'");
        Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).setFields("sheets.properties").execute();
        return spreadsheet.getSheets().stream()
                .map(sheet -> sheet.getProperties())
                .filter(sheetProperties -> unescapedPage.equals(sheetProperties.getTitle()))
                .map(sheetProperties -> sheetProperties.getSheetId())
                .findFirst()
                .orElseThrow(GoogleSheetsException::new);
    }

    static String columnLetter(int columnCount) {
        StringBuilder columnName = new StringBuilder();
        int remaining = Math.max(columnCount, 1);
        while (remaining > 0) {
            int rem = (remaining - 1) % 26;
            columnName.insert(0, (char) ('A' + rem));
            remaining = (remaining - 1) / 26;
        }
        return columnName.toString();
    }
}
