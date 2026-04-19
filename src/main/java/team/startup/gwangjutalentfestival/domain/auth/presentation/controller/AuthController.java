package team.startup.gwangjutalentfestival.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.LoginRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SignUpRequest;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.auth.service.LoginService;
import team.startup.gwangjutalentfestival.domain.auth.service.LogoutService;
import team.startup.gwangjutalentfestival.domain.auth.service.ReissueTokenService;
import team.startup.gwangjutalentfestival.domain.auth.service.SendVerifyCodeService;
import team.startup.gwangjutalentfestival.domain.auth.service.SignUpService;

/**
 * 인증 관련 API 엔드포인트를 처리하는 컨트롤러.
 * <p>인증번호 발송, 회원가입, 로그인, 로그아웃, 토큰 재발급 기능을 제공합니다.</p>
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SendVerifyCodeService sendVerifyCodeService;
    private final SignUpService signUpService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ReissueTokenService reissueTokenService;

    /**
     * 입력한 휴대폰 번호로 SMS 인증번호를 발송합니다.
     *
     * @param request 인증번호를 발송할 휴대폰 번호 요청 객체
     * @return 204 No Content
     */
    @Operation(summary = "인증번호 발송", description = "입력한 휴대폰 번호로 SMS 인증번호를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "인증번호 발송 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 휴대폰 번호 형식"),
            @ApiResponse(responseCode = "429", description = "인증 요청 횟수 초과")
    })
    @PostMapping("/verify")
    public ResponseEntity<Void> sendVerifyCode(@RequestBody @Valid SendVerifyCodeRequest request) {
        sendVerifyCodeService.execute(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 휴대폰 인증 후 비밀번호를 설정하여 회원가입합니다.
     *
     * @param request 휴대폰 번호, 비밀번호, 인증번호를 포함한 회원가입 요청 객체
     * @return 201 Created
     */
    @Operation(summary = "회원가입", description = "휴대폰 인증 후 비밀번호를 설정하여 회원가입합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청값 (번호 형식, 비밀번호 규칙 등)"),
            @ApiResponse(responseCode = "401", description = "인증번호 불일치 또는 만료"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 휴대폰 번호")
    })
    @PostMapping("/join")
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest request) {
        signUpService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 휴대폰 번호와 비밀번호로 로그인하고 JWT 토큰을 반환합니다.
     *
     * @param request 휴대폰 번호와 비밀번호를 포함한 로그인 요청 객체
     * @return AccessToken 및 RefreshToken을 포함한 {@link TokenResponse}
     */
    @Operation(summary = "로그인", description = "휴대폰 번호와 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 휴대폰 번호 형식"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(loginService.execute(request));
    }

    /**
     * 현재 로그인된 사용자의 RefreshToken을 삭제하고 AccessToken을 블랙리스트에 등록합니다.
     *
     * @param authorization Bearer 형식의 AccessToken이 담긴 Authorization 헤더
     * @return 204 No Content
     */
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 RefreshToken을 삭제합니다.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String bearer = authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
        logoutService.execute(bearer);
        return ResponseEntity.noContent().build();
    }

    /**
     * RefreshToken으로 새로운 AccessToken과 RefreshToken을 재발급합니다.
     *
     * @param refreshToken Refresh-Token 헤더에 담긴 RefreshToken 문자열
     * @return 새로 발급된 AccessToken 및 RefreshToken을 포함한 {@link TokenResponse}
     */
    @Operation(summary = "토큰 재발급", description = "RefreshToken으로 새로운 AccessToken과 RefreshToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 RefreshToken")
    })
    @PatchMapping("/refresh")
    public ResponseEntity<TokenResponse> reissue(
            @Parameter(description = "RefreshToken", required = true)
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(reissueTokenService.execute(refreshToken));
    }
}
