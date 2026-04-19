package team.startup.gwangjutalentfestival.domain.slogan.presentation.data;

/**
 * Google Sheets에 슬로건 데이터를 추가할 때 사용하는 행 데이터 DTO.
 *
 * @param slogan      슬로건 문구
 * @param description 슬로건 설명
 * @param school      학교명
 * @param name        제출자 이름
 * @param grade       학년
 * @param classNum    반 번호
 * @param phoneNumber 전화번호
 */
public record SloganSheetRowData(
        String slogan,

        String description,

        String school,

        String name,

        Integer grade,

        Integer classNum,

        String phoneNumber
) {
}
