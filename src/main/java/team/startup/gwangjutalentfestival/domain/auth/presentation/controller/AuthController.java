package team.startup.gwangjutalentfestival.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.OAuthLoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.service.KaKaoOAuthLoginService;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KaKaoOAuthLoginService kaKaoOAuthLoginService;

    @Operation(summary = "카카오 OAuth 로그인", description = "카카오 인가 코드로 로그인 후 JWT 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 OAuth 제공자"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/oauth")
    public ResponseEntity<TokenResponse> login(@RequestBody OAuthLoginRequest request) {
        TokenResponse response = kaKaoOAuthLoginService.execute(request);
        return ResponseEntity.ok(response);
    }
}
