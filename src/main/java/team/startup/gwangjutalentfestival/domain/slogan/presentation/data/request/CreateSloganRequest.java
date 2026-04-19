package team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request;

import jakarta.validation.constraints.*;

/**
 * 슬로건 등록 요청 DTO.
 *
 * @param slogan      슬로건 문구 (필수)
 * @param description 슬로건 설명 (필수)
 * @param school      학교명 (필수)
 * @param name        제출자 이름 (필수)
 * @param grade       학년 (1~6)
 * @param classNum    반 번호 (양수)
 * @param phoneNumber 전화번호 (010으로 시작하는 11자리 숫자)
 */
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
