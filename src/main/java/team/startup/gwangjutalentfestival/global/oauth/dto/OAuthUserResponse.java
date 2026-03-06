package team.startup.gwangjutalentfestival.global.oauth.dto;

import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.oauth.common.OAuthType;

public record OAuthUserResponse(
        String providerId,
        OAuthType type,
        String email,
        Role role
) {
}
