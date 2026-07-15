package team.startup.gwangjutalentfestival.domain.apply.presentation.data.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드 완료된 파트 정보")
public record ApplyPartInput(
        @Schema(description = "파트 번호(업로드한 파트와 동일)", example = "1")
        int partNumber,
        @Schema(description = "해당 파트를 S3에 PUT한 응답의 ETag(큰따옴표 포함)", example = "\"0565fb7f2b41a97b137c57a002caaa80\"")
        String etag
) {
}
