package team.startup.gwangjutalentfestival.domain.apply.presentation.data.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파트별 업로드 URL")
public record ApplyPartUrl(
        @Schema(description = "파트 번호(1부터 시작)", example = "1")
        int partNumber,
        @Schema(description = "해당 파트를 S3에 직접 PUT 업로드할 Presigned URL", example = "https://gwangtalfae-s3.s3.ap-northeast-2.amazonaws.com/videos/...?partNumber=1&uploadId=...&X-Amz-Signature=...")
        String url
) {
}
