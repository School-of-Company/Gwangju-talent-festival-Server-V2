package team.startup.gwangjutalentfestival.domain.apply.presentation.data.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공연 신청 완료 응답")
public record ApplyResponse(
        @Schema(description = "생성된 신청 ID. 이후 영상 다운로드 링크 조회에 사용한다.", example = "15")
        Long applyId
) {
}
