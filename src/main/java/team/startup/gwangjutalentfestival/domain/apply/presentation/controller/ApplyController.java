package team.startup.gwangjutalentfestival.domain.apply.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyVideoUrlService;

@Tag(name = "Apply", description = "신청 API")
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;
    private final GetApplyVideoUrlService getApplyVideoUrlService;

    @Operation(summary = "공연 신청", description = "팀 공연 영상(MP4)을 업로드하고 신청 ID와 다운로드 링크를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 영상 파일"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplyResponse> apply(
            @Parameter(description = "업로드할 MP4 파일", required = true)
            @RequestPart MultipartFile file) {
        return ResponseEntity.ok(applyService.execute(file));
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
