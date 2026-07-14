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
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyAbortRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyCompleteRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartUrlsRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyPartUrlsResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadInitiateResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.AbortApplyUploadService;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyPartUrlsService;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyVideoUrlService;
import team.startup.gwangjutalentfestival.domain.apply.service.InitiateApplyUploadService;

@Tag(name = "Apply", description = "신청 API")
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final InitiateApplyUploadService initiateApplyUploadService;
    private final GetApplyPartUrlsService getApplyPartUrlsService;
    private final ApplyService applyService;
    private final AbortApplyUploadService abortApplyUploadService;
    private final GetApplyVideoUrlService getApplyVideoUrlService;

    @Operation(summary = "멀티파트 업로드 시작", description = "영상 멀티파트 업로드를 시작하고 S3 객체 key와 uploadId를 발급합니다. 이후 part-urls로 파트별 업로드 URL을 받아 클라이언트가 S3에 직접 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 시작 성공")
    })
    @PostMapping("/upload/initiate")
    public ResponseEntity<ApplyUploadInitiateResponse> initiateUpload() {
        return ResponseEntity.ok(initiateApplyUploadService.execute());
    }

    @Operation(summary = "파트 업로드 URL 발급", description = "각 파트 번호에 대한 업로드용 Presigned URL을 발급합니다. 클라이언트는 파일을 partCount개로 분할해 각 URL로 PUT 업로드하고, 응답 헤더의 ETag를 수집합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청")
    })
    @PostMapping("/upload/part-urls")
    public ResponseEntity<ApplyPartUrlsResponse> getPartUrls(@RequestBody ApplyPartUrlsRequest request) {
        return ResponseEntity.ok(getApplyPartUrlsService.execute(request));
    }

    @Operation(summary = "공연 신청 (업로드 완료)", description = "업로드한 파트들을 합쳐 멀티파트 업로드를 완료하고, MP4 검증 후 신청을 확정합니다. parts에는 각 파트의 partNumber와 ETag를 담습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 영상 파일"),
            @ApiResponse(responseCode = "500", description = "업로드 완료 처리 실패")
    })
    @PostMapping
    public ResponseEntity<ApplyResponse> complete(@RequestBody ApplyCompleteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applyService.execute(request));
    }

    @Operation(summary = "업로드 취소", description = "진행 중인 멀티파트 업로드를 취소하고 업로드된 파트를 정리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청")
    })
    @PostMapping("/upload/abort")
    public ResponseEntity<Void> abort(@RequestBody ApplyAbortRequest request) {
        abortApplyUploadService.execute(request);
        return ResponseEntity.noContent().build();
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
