package team.startup.gwangjutalentfestival.domain.slogan.dto.request;

import jakarta.validation.constraints.*;

public record CreateSloganRequest(
        @NotBlank
        String slogan,

        @NotBlank
        String description,

        @NotBlank
        String school,

        @NotBlank
        String name,

        @Min(1)
        @Max(6)
        @NotNull
        Integer grade,

        @NotNull
        @Positive
        Integer classNum,

        @Pattern(regexp = "^010\\d{8}$")
        String phoneNumber
) {
}
