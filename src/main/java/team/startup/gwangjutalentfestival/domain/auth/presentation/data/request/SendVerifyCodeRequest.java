package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * SMS 인증번호 발송 요청 DTO.
 *
 * @param phoneNumber 인증번호를 받을 휴대폰 번호 (010으로 시작하는 11자리)
 */
public record SendVerifyCodeRequest(
        @NotNull(message = "휴대폰 번호는 필수입니다.")
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "유효한 휴대폰 번호 형식이 아닙니다."
        )
        String phoneNumber
) {
}
