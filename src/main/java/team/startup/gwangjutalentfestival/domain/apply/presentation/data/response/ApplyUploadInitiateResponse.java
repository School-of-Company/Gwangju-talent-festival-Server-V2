package team.startup.gwangjutalentfestival.domain.apply.presentation.data.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멀티파트 업로드 시작 응답")
public record ApplyUploadInitiateResponse(
        @Schema(description = "발급된 S3 객체 key. 이후 part-urls·complete·abort에 그대로 전달한다.", example = "videos/3ae27271-2e0b-42cb-86cb-142ab1ba0c8c.mp4")
        String key,
        @Schema(description = "멀티파트 업로드 식별자(uploadId). 이후 모든 단계에 전달한다.", example = "tEJS1bMFUUo3A7.q8r6IQOkpKVildL1p...")
        String uploadId
) {
}
