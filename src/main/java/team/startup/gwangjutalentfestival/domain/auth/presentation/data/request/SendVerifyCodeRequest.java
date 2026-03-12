package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

import jakarta.validation.constraints.Pattern;

public record SendVerifyCodeRequest(
        @Pattern(
                regexp = "^01[0-9]{8,9}$",
                message = "유효한 휴대폰 번호 형식이 아닙니다."
        )
        String phoneNumber
) {
}
