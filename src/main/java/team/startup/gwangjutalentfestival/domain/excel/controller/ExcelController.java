package team.startup.gwangjutalentfestival.domain.excel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;

import java.nio.charset.StandardCharsets;

@Tag(name = "Excel", description = "엑셀 다운로드 API")
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final DownloadJudgingSummaryExcelService downloadJudgingSummaryExcelService;

    @Operation(summary = "심사 집계표 다운로드", description = "전체 팀의 심사 집계 결과를 xlsx 파일로 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공")
    })
    @GetMapping("/summary")
    public ResponseEntity<byte[]> downloadSummary() {
        byte[] file = downloadJudgingSummaryExcelService.execute();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("심사집계표.xlsx", StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }
}
