package team.startup.gwangjutalentfestival.domain.auth.event;

public record VerifyCodeCreatedEvent(
        String phoneNumber,
        String code
) {
}
