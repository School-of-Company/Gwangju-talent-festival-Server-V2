package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.SloganSheetRowData;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsApiException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsIoException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleSheetsProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

/**
 * Google Sheets API 연동 어댑터.
 * <p>슬로건 데이터를 Google Sheets에 행(row) 단위로 추가하는 기능을 제공한다.
 * API 오류, IO 오류, 예상치 못한 오류를 각각 도메인 예외로 변환하여 던진다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsAdapter {

    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    /**
     * 슬로건 데이터 목록을 Google Sheets에 추가한다.
     *
     * @param data 추가할 슬로건 행 데이터 목록
     * @throws GoogleSheetsApiException Google Sheets API 오류 발생 시
     * @throws GoogleSheetsIoException  네트워크/IO 오류 발생 시
     * @throws GoogleSheetsException    그 외 예기치 못한 오류 발생 시
     */
    public void appendSlogan(List<SloganSheetRowData> data) {
        List<List<Object>> rows = getList(data);
        String sheetId = properties.sheetId();
        String sheetPage = properties.sheetPage();

        try {
            ValueRange valueRange = new ValueRange().setValues(rows);

            sheets.spreadsheets().values()
                    .append(sheetId, sheetPage, valueRange)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

        } catch (GoogleJsonResponseException e) {
            log.error("Google Sheets API 오류 발생 - statusCode: {}, message: {}", e.getStatusCode(), e.getMessage());
            throw new GoogleSheetsApiException();
        } catch (IOException e) {
            log.error("Google Sheets IO 오류 발생 - message: {}", e.getMessage());
            throw new GoogleSheetsIoException();
        } catch (Exception e) {
            log.error("Google Sheets 예기치 못한 오류 발생 - message: {}", e.getMessage());
            throw new GoogleSheetsException();
        }
    }

    private List<List<Object>> getList(List<SloganSheetRowData> data) {
        return data.stream()
                .map(s -> java.util.Arrays.<Object>asList(
                        s.slogan(),
                        s.description(),
                        s.school(),
                        s.name(),
                        s.grade(),
                        s.classNum(),
                        s.phoneNumber(),
                        s.birthDate() != null ? s.birthDate().toString() : null
                ))
                .toList();
    }
}