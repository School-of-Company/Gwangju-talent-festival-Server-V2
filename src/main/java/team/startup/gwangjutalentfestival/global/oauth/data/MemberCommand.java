package team.startup.gwangjutalentfestival.global.oauth.data;

import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;
import team.startup.gwangjutalentfestival.global.oauth.dto.OAuthUserResponse;

public record MemberCommand(
    String email,
    String providerId,
    OAuthType type
) {
    public static MemberCommand from(OAuthUserResponse response) {
        return new MemberCommand(
                response.email(),
                response.providerId(),
                response.type()
        );
    }
}
