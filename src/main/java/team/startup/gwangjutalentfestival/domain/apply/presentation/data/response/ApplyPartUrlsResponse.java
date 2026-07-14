package team.startup.gwangjutalentfestival.domain.apply.presentation.data.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "파트 업로드 URL 발급 응답")
public record ApplyPartUrlsResponse(
        @Schema(description = "파트 번호별 업로드 URL 목록. 클라이언트는 각 URL로 해당 파트를 PUT 업로드하고 응답 헤더의 ETag를 수집한다.")
        List<ApplyPartUrl> parts
) {
}
