package team.startup.gwangjutalentfestival.global.thirdparty.google.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Google Sheets 연동 설정 프로퍼티.
 * <p>{@code google.sheets.*} 프로퍼티로 서비스 계정 자격증명, 스프레드시트 ID, 시트 페이지를 바인딩한다.</p>
 *
 * @param accountCredential    서비스 계정 JSON 자격증명 문자열
 * @param sheetId              Google 스프레드시트 ID
 * @param enrolledSheetPage    재학생 데이터를 기록할 시트 탭 이름
 * @param outOfSchoolSheetPage 학교 밖 청소년 데이터를 기록할 시트 탭 이름
 */
@ConfigurationProperties(prefix = "google.sheets")
public record GoogleSheetsProperties(
        String accountCredential,
        String sheetId,
        String enrolledSheetPage,
        String outOfSchoolSheetPage
) {
}
