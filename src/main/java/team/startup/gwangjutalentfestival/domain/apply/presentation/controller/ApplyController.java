package team.startup.gwangjutalentfestival.domain.apply.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyUploadUrlService;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyVideoUrlService;

@Tag(name = "Apply", description = "신청 API")
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final GetApplyUploadUrlService getApplyUploadUrlService;
    private final ApplyService applyService;
    private final GetApplyVideoUrlService getApplyVideoUrlService;

    @Operation(summary = "영상 업로드 URL 발급", description = "영상을 S3에 직접 업로드할 수 있는 Presigned URL을 발급합니다. 클라이언트는 반환된 uploadUrl로 Content-Type: video/mp4 헤더와 함께 PUT 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공")
    })
    @PostMapping("/upload-url")
    public ResponseEntity<ApplyUploadUrlResponse> getUploadUrl() {
        return ResponseEntity.ok(getApplyUploadUrlService.execute());
    }

    @Operation(summary = "공연 신청", description = "S3 업로드를 마친 영상의 key로 신청을 확정합니다. 업로드된 파일이 MP4인지 검증한 뒤 신청 ID를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 영상 파일")
    })
    @PostMapping
    public ResponseEntity<ApplyResponse> apply(@RequestBody ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applyService.execute(request));
    }

    @Operation(summary = "공연 영상 다운로드 링크 조회", description = "신청 ID로 10분간 유효한 영상 다운로드 링크를 발급합니다. 만료 시 다시 호출하면 새 링크가 발급됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "링크 발급 성공"),
            @ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음")
    })
    @GetMapping("/{applyId}/video")
    public ResponseEntity<ApplyVideoUrlResponse> getVideoUrl(
            @Parameter(description = "신청 ID", required = true)
            @PathVariable Long applyId) {
        return ResponseEntity.ok(getApplyVideoUrlService.execute(applyId));
    }
}
