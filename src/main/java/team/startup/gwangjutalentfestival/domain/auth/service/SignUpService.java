package team.startup.gwangjutalentfestival.domain.auth.service;

import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SignUpRequest;

/**
 * 회원가입 처리 서비스 인터페이스.
 */
public interface SignUpService {
    /**
     * 인증번호를 검증하고 신규 회원을 등록합니다.
     *
     * @param request 휴대폰 번호, 비밀번호, 인증번호를 포함한 회원가입 요청 정보
     */
    void execute(SignUpRequest request);
}
