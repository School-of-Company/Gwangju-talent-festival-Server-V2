package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "유효한 휴대폰 번호 형식이 아닙니다."
        )
        String phoneNumber,

        @NotNull
        String password
) {
}
