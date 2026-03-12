package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.slogan.dto.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsApiException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsIoException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleSheetsProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsAdapter {

    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    public void appendSlogan(CreateSloganRequest request) {
        List<List<Object>> rows = getList(request);
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

    private List<List<Object>> getList(CreateSloganRequest request){
        List<Object> sloganList = List.of(
                request.slogan(),
                request.description(),
                request.school(),
                request.name(),
                request.grade(),
                request.classNum(),
                request.phoneNumber()
        );
        return List.of(sloganList);
    }
}