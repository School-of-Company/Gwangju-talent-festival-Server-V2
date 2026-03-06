package team.startup.gwangjutalentfestival.auth.presentation.data.request;

public record OAuthLoginRequest(
        String code,
        String provider,
        String redirectUri
) {
}
