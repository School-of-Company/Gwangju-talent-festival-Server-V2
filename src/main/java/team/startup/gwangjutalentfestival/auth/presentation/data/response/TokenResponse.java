package team.startup.gwangjutalentfestival.auth.presentation.data.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.time.LocalDateTime;

public record TokenResponse(
        String accessToken,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime accessTokenExpiresAt,
        String refreshToken,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime refreshTokenExpiresAt,
        Role role
) {
}
