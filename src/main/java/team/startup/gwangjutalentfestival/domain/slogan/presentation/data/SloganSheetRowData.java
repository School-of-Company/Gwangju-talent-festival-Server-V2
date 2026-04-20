package team.startup.gwangjutalentfestival.domain.slogan.presentation.data;

import java.time.LocalDate;

public record SloganSheetRowData(
        String slogan,
        String description,
        String school,
        String name,
        Integer grade,
        Integer classNum,
        String phoneNumber,
        LocalDate birthDate
) {
}