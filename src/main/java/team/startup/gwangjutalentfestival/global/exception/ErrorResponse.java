package team.startup.gwangjutalentfestival.global.exception;

public record ErrorResponse(
        int status,
        String message
) {
}
