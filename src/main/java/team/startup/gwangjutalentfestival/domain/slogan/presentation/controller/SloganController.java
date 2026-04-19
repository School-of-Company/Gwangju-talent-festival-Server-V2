package team.startup.gwangjutalentfestival.domain.slogan.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.domain.slogan.service.CreateSloganService;

/**
 * 슬로건 관련 API 엔드포인트를 제공하는 컨트롤러.
 */
@Tag(name = "Slogan", description = "슬로건 API")
@RestController
@RequestMapping("/slogan")
@RequiredArgsConstructor
public class SloganController {

    private final CreateSloganService createSloganService;

    @Operation(summary = "슬로건 등록", description = "슬로건을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "슬로건 등록 성공")
    })
    /**
     * 슬로건을 등록한다.
     *
     * @param request 슬로건 등록 요청 데이터
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Void> createSlogan(@Valid @RequestBody CreateSloganRequest request) {
        createSloganService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}