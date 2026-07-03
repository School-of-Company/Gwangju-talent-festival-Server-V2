package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;
import team.startup.gwangjutalentfestival.domain.auth.exception.ExpiredVerifyCodeException;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidVerifyCodeException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SignUpRequest;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.SignUpService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.auth.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

/**
 * {@link SignUpService} 구현체.
 * <p>인증번호 검증, 휴대폰 번호 중복 확인 후 신규 회원을 등록합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {

    private final UserRepository userRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증번호를 검증하고 휴대폰 번호 중복 확인 후 신규 회원을 저장합니다.
     *
     * @param request 휴대폰 번호, 비밀번호, 인증번호를 포함한 회원가입 요청 정보
     * @throws ExpiredVerifyCodeException    인증번호가 만료된 경우
     * @throws InvalidVerifyCodeException    입력한 인증번호가 일치하지 않는 경우
     * @throws DuplicatePhoneNumberException 이미 가입된 휴대폰 번호인 경우
     */
    @Override
    @Transactional
    public void execute(SignUpRequest request) {
        VerifyCode code = verifyCodeRepository.findById(request.phoneNumber())
                .orElseThrow(ExpiredVerifyCodeException::new);

        if (!code.getCode().equals(request.code())) {
            throw new InvalidVerifyCodeException();
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicatePhoneNumberException();
        }

        UserEntity user = UserEntity.builder()
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        verifyCodeRepository.delete(code);

        userRepository.save(user);
    }
}
