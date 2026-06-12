package team.startup.gwangjutalentfestival.global.s3.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class S3UploadFailedException extends GlobalException {
    public S3UploadFailedException() {
        super(ErrorCode.S3_UPLOAD_FAILED);
    }
}
