package team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request;

import jakarta.validation.constraints.*;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SchoolStatus;

import java.time.LocalDate;

public record CreateSloganRequest(
        @NotBlank
        String name,

        @Pattern(regexp = "^010\\d{8}$")
        @NotBlank
        String phoneNumber,

        @NotNull
        SchoolStatus schoolStatus,

        // ENROLLED 전용
        String slogan,
        String description,
        String school,

        @Min(1)
        @Max(6)
        Integer grade,

        @Positive
        Integer classNum,

        LocalDate birthDate
) {
}