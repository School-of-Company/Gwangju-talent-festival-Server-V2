package team.startup.gwangjutalentfestival.global.s3.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidVideoFileException extends GlobalException {
    public InvalidVideoFileException() {
        super(ErrorCode.INVALID_VIDEO_FILE);
    }
}
