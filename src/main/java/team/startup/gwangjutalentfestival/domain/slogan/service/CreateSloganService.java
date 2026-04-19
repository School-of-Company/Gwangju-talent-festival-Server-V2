package team.startup.gwangjutalentfestival.domain.slogan.service;

import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request.CreateSloganRequest;

/**
 * 슬로건을 등록하는 서비스 인터페이스.
 */
public interface CreateSloganService {

    /**
     * 슬로건을 등록한다.
     * 접수 기간 및 전화번호 중복 여부를 검증한 후 저장한다.
     *
     * @param request 슬로건 등록 요청 데이터
     */
    void execute(CreateSloganRequest request);
}
