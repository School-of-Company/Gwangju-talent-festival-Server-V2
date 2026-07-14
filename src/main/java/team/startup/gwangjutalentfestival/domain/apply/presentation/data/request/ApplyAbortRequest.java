package team.startup.gwangjutalentfestival.domain.apply.presentation.data.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멀티파트 업로드 취소 요청")
public record ApplyAbortRequest(
        @Schema(description = "initiate에서 받은 S3 객체 key", example = "videos/3ae27271-2e0b-42cb-86cb-142ab1ba0c8c.mp4")
        String key,
        @Schema(description = "취소할 멀티파트 업로드의 uploadId", example = "tEJS1bMFUUo3A7.q8r6IQOkpKVildL1p...")
        String uploadId
) {
}
