package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleExcelAdapter {

    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Sheets sheets;
    private final Drive drive;
    private final GoogleExcelProperties properties;

    public synchronized byte[] exportSummary(List<List<Object>> rows) {
        String sheetId = properties.templateSheetId();
        String page = properties.summaryPage().replace("'", "''");
        String writeRange = "'" + page + "'!A3";
        String clearRange = "'" + page + "'!A3:J" + (2 + rows.size());
        try {
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
                        .clear(sheetId, clearRange, new com.google.api.services.sheets.v4.model.ClearValuesRequest())
                        .execute();
            } catch (Exception e) {
                log.error("Google Sheets 데이터 초기화 실패 - message: {}", e.getMessage());
            }
        }
    }
}
