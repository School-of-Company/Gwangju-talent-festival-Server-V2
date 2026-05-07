package team.startup.gwangjutalentfestival.domain.slogan.presentation.data;

import java.time.LocalDate;

public record OutOfSchoolSloganSheetRowData(
        String slogan,
        String description,
        String name,
        String phoneNumber,
        LocalDate birthDate
) {
}
