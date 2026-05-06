package team.startup.gwangjutalentfestival.global.thirdparty.google.adapter;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.EnrolledSloganSheetRowData;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.OutOfSchoolSloganSheetRowData;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsApiException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsIoException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleSheetsProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsAdapter {

    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    public void appendEnrolledSlogan(List<EnrolledSloganSheetRowData> data) {
        append(properties.enrolledSheetPage(), toEnrolledRows(data));
    }

    public void appendOutOfSchoolSlogan(List<OutOfSchoolSloganSheetRowData> data) {
        append(properties.outOfSchoolSheetPage(), toOutOfSchoolRows(data));
    }

    private void append(String sheetPage, List<List<Object>> rows) {
        try {
            ValueRange valueRange = new ValueRange().setValues(rows);

            sheets.spreadsheets().values()
                    .append(properties.sheetId(), sheetPage, valueRange)
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

    private List<List<Object>> toEnrolledRows(List<EnrolledSloganSheetRowData> data) {
        return data.stream()
                .map(s -> Arrays.<Object>asList(
                        s.slogan(),
                        s.description(),
                        s.school(),
                        s.grade(),
                        s.classNum(),
                        s.name(),
                        s.phoneNumber()
                ))
                .toList();
    }

    private List<List<Object>> toOutOfSchoolRows(List<OutOfSchoolSloganSheetRowData> data) {
        return data.stream()
                .map(s -> Arrays.<Object>asList(
                        s.slogan(),
                        s.description(),
                        s.name(),
                        s.phoneNumber(),
                        s.birthDate().toString()
                ))
                .toList();
    }
}