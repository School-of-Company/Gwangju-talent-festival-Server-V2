package team.startup.gwangjutalentfestival.domain.performer.presentation.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPerformerRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name,

        @NotBlank(message = "인증코드를 입력해주세요.")
        @Size(max = 200, message = "인증코드는 200자 이하여야 합니다.")
        String code
) {
}
