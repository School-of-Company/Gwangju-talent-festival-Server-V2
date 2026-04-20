package team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request;

import jakarta.validation.constraints.*;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SchoolStatus;

import java.time.LocalDate;

/**
 * 슬로건 등록 요청 DTO.
 *
 * @param name         제출자 이름 (필수)
 * @param phoneNumber  전화번호 (010으로 시작하는 11자리 숫자, 필수)
 * @param schoolStatus 학교 재학 상태 (필수)
 * @param slogan       슬로건 문구 (필수)
 * @param description  슬로건 설명 (필수)
 * @param school       학교명 (ENROLLED 필수)
 * @param grade        학년 1~6 (ENROLLED 필수)
 * @param classNum     반 번호 (ENROLLED 필수)
 * @param birthDate    생년월일 (OUT_OF_SCHOOL 필수)
 */
public record CreateSloganRequest(
        @NotBlank
        String name,

        @Pattern(regexp = "^010\\d{8}$")
        @NotBlank
        String phoneNumber,

        @NotBlank
        SchoolStatus schoolStatus,

        @NotBlank
        String slogan,

        @NotBlank
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