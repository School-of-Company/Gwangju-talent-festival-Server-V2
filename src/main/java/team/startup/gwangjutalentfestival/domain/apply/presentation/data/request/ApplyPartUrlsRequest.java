package team.startup.gwangjutalentfestival.domain.apply.presentation.data.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파트 업로드 URL 발급 요청")
public record ApplyPartUrlsRequest(
        @Schema(description = "initiate에서 받은 S3 객체 key", example = "videos/3ae27271-2e0b-42cb-86cb-142ab1ba0c8c.mp4")
        String key,
        @Schema(description = "initiate에서 받은 uploadId", example = "tEJS1bMFUUo3A7.q8r6IQOkpKVildL1p...")
        String uploadId,
        @Schema(description = "파일을 나눌 파트 개수(1 이상, 최대 10000). 클라이언트가 파일 크기/청크 크기로 계산한다.", example = "3")
        int partCount
) {
}
