package team.startup.gwangjutalentfestival.domain.apply.presentation.data.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영상 다운로드 링크 응답")
public record ApplyVideoUrlResponse(
        @Schema(description = "10분간 유효한 영상 다운로드 Presigned URL. 만료 시 다시 조회하면 새로 발급된다.", example = "https://gwangtalfae-s3.s3.ap-northeast-2.amazonaws.com/videos/...?X-Amz-Signature=...")
        String videoUrl
) {
}
