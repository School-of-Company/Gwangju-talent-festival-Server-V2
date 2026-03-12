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
import team.startup.gwangjutalentfestival.domain.user.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {

    private final UserRepository userRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(SignUpRequest request) {
        VerifyCode code = verifyCodeRepository.findById(request.phoneNumber())
                .orElseThrow(ExpiredVerifyCodeException::new);

        if (!code.getCode().equals(request.code())) {
            throw new InvalidVerifyCodeException();
        }

        if(userRepository.existsByPhoneNumber(request.phoneNumber())){
            throw new DuplicatePhoneNumberException();
        }

        UserEntity user = UserEntity.builder()
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        verifyCodeRepository.delete(code);
    }

}

