package team.startup.gwangjutalentfestival.domain.performer.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class PerformerVerificationAlreadyClaimedException extends GlobalException {
    public PerformerVerificationAlreadyClaimedException() {
        super(ErrorCode.PERFORMER_VERIFICATION_ALREADY_CLAIMED);
    }
}
