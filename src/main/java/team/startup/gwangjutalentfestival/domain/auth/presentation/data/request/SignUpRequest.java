package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 회원가입 요청 DTO.
 *
 * @param phoneNumber 가입에 사용할 휴대폰 번호 (010으로 시작하는 11자리)
 * @param password    계정 비밀번호 (영문, 숫자, 특수문자 포함 8자리 이상)
 * @param code        SMS로 수신한 인증번호
 */
public record SignUpRequest(
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "유효한 휴대폰 번호 형식이 아닙니다."
        )
        String phoneNumber,

        @Pattern(
                regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
                message = "특수문자, 영문과 숫자를 포함한 8자리 이상의 비밀번호를 만들어주세요."
        )
        String password,

         @NotBlank(message = "인증번호를 입력해주세요.")
        String code
) {
}
