package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;

/**
 * SMS 인증번호 발송 서비스 인터페이스.
 */
public interface SendVerifyCodeService {
    /**
     * 입력한 휴대폰 번호로 SMS 인증번호를 생성하고 발송합니다.
     *
     * @param request 인증번호를 발송할 휴대폰 번호 요청 정보
     */
    void execute(SendVerifyCodeRequest request);
}
