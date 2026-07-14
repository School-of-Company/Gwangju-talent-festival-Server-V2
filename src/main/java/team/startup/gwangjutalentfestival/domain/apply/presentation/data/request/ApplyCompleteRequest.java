package team.startup.gwangjutalentfestival.domain.apply.presentation.data.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "공연 신청(멀티파트 업로드 완료) 요청")
public record ApplyCompleteRequest(
        @Schema(description = "initiate에서 받은 S3 객체 key", example = "videos/3ae27271-2e0b-42cb-86cb-142ab1ba0c8c.mp4")
        String key,
        @Schema(description = "initiate에서 받은 uploadId", example = "tEJS1bMFUUo3A7.q8r6IQOkpKVildL1p...")
        String uploadId,
        @Schema(description = "원본 파일명. 저장 시 확장자는 .mp4로 정규화된다.", example = "공연영상.mp4")
        String filename,
        @Schema(description = "업로드한 모든 파트의 번호와 ETag 목록")
        List<ApplyPartInput> parts
) {
}
