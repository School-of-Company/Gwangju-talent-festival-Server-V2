package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 로그인 요청 DTO.
 *
 * @param phoneNumber 로그인에 사용할 휴대폰 번호 (010으로 시작하는 11자리)
 * @param password    계정 비밀번호
 */
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
