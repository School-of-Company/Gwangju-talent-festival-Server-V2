package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;
import team.startup.gwangjutalentfestival.domain.auth.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.auth.exception.ExpiredVerifyCodeException;
import team.startup.gwangjutalentfestival.domain.auth.exception.InvalidVerifyCodeException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SignUpRequest;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignUpServiceImplTest {

    private static final String PHONE_NUMBER = "01012345678";
    private static final String CODE = "123456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerifyCodeRepository verifyCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SignUpServiceImpl service() {
        return new SignUpServiceImpl(userRepository, verifyCodeRepository, passwordEncoder);
    }

    private SignUpRequest request() {
        return new SignUpRequest(PHONE_NUMBER, "password1!", CODE);
    }

    private VerifyCode verifyCode(String code) {
        return VerifyCode.builder()
                .phoneNumber(PHONE_NUMBER)
                .code(code)
                .ttl(180L)
                .build();
    }

    @Test
    void 정상_회원가입_요청시_유저가_저장되고_인증번호가_삭제된다() {
        VerifyCode code = verifyCode(CODE);
        given(verifyCodeRepository.findById(PHONE_NUMBER)).willReturn(Optional.of(code));
        given(userRepository.existsByPhoneNumber(PHONE_NUMBER)).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");

        service().execute(request());

        verify(verifyCodeRepository).delete(code);
        verify(userRepository).saveAndFlush(any());
    }

    @Test
    void 인증번호가_존재하지_않으면_ExpiredVerifyCodeException이_발생한다() {
        given(verifyCodeRepository.findById(PHONE_NUMBER)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(ExpiredVerifyCodeException.class);
    }

    @Test
    void 인증번호가_일치하지_않으면_InvalidVerifyCodeException이_발생한다() {
        given(verifyCodeRepository.findById(PHONE_NUMBER)).willReturn(Optional.of(verifyCode("000000")));

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(InvalidVerifyCodeException.class);
    }

    @Test
    void 이미_가입된_전화번호면_DuplicatePhoneNumberException이_발생한다() {
        given(verifyCodeRepository.findById(PHONE_NUMBER)).willReturn(Optional.of(verifyCode(CODE)));
        given(userRepository.existsByPhoneNumber(PHONE_NUMBER)).willReturn(true);

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(DuplicatePhoneNumberException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 동시_가입_요청으로_DataIntegrityViolation_발생시_DuplicatePhoneNumberException이_발생한다() {
        given(verifyCodeRepository.findById(PHONE_NUMBER)).willReturn(Optional.of(verifyCode(CODE)));
        given(userRepository.existsByPhoneNumber(PHONE_NUMBER)).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        given(userRepository.saveAndFlush(any())).willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service().execute(request()))
                .isInstanceOf(DuplicatePhoneNumberException.class);
    }
}
