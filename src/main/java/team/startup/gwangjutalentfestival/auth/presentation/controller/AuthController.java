package team.startup.gwangjutalentfestival.auth.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.auth.presentation.data.request.OAuthLoginRequest;
import team.startup.gwangjutalentfestival.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.auth.service.KaKaoOAuthLoginService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KaKaoOAuthLoginService kaKaoOAuthLoginService;

    @PostMapping("/oauth")
    public ResponseEntity<TokenResponse> login(@RequestBody OAuthLoginRequest request){
        TokenResponse response = kaKaoOAuthLoginService.execute(request);
        return ResponseEntity.ok(response);
    }
}
