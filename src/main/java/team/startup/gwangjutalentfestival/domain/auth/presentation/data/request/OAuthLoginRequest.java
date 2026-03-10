package team.startup.gwangjutalentfestival.domain.auth.presentation.data.request;

public record OAuthLoginRequest(
        String code,
        String provider,
        String redirectUri
) {
}
