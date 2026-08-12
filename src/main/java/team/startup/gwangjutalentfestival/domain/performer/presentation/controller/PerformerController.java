package team.startup.gwangjutalentfestival.domain.performer.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.performer.presentation.data.request.VerifyPerformerRequest;
import team.startup.gwangjutalentfestival.domain.performer.service.VerifyPerformerService;
import team.startup.gwangjutalentfestival.global.ratelimit.RateLimited;

@Tag(name = "Performer", description = "출연진 인증 API")
@RestController
@RequestMapping("/performer")
@RequiredArgsConstructor
public class PerformerController {

    private final VerifyPerformerService verifyPerformerService;

    @Operation(summary = "출연진 인증", description = "일회성 인증코드로 현재 계정에 출연진 권한을 부여합니다.")
    @SecurityRequirement(name = "Authorization")
    @RateLimited(key = "performer:verification")
    @PostMapping("/verify")
    public ResponseEntity<TokenResponse> verify(@RequestBody @Valid VerifyPerformerRequest request) {
        return ResponseEntity.ok(verifyPerformerService.execute(request));
    }
}
