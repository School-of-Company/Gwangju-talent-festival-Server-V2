package team.startup.gwangjutalentfestival.global.oauth.data;

public record ProviderProperties(
        String clientId,
        String clientSecret,
        String tokenUri,
        String userInfoUri
) {
}
