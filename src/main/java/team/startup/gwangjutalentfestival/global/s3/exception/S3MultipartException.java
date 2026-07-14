package team.startup.gwangjutalentfestival.global.s3.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class S3MultipartException extends GlobalException {
    public S3MultipartException() {
        super(ErrorCode.MULTIPART_UPLOAD_FAILED);
    }
}
