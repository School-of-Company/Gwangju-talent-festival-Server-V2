package team.startup.gwangjutalentfestival.domain.judge.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 필기 코멘트 strokes 페이로드가 허용 크기를 초과할 때 발생하는 예외.
 */
public class JudgeCommentTooLargeException extends GlobalException {
    public JudgeCommentTooLargeException() {
        super(ErrorCode.JUDGE_COMMENT_TOO_LARGE);
    }
}