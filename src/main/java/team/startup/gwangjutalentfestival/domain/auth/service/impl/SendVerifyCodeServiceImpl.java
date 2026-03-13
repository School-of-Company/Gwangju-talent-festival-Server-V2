package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;
import team.startup.gwangjutalentfestival.domain.auth.exception.AlreadyVerifyCodeExistsException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.SendVerifyCodeService;
import team.startup.gwangjutalentfestival.global.sms.adapter.SmsAdapter;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;
import team.startup.gwangjutalentfestival.global.util.RandomUtil;

@Service
@RequiredArgsConstructor
public class SendVerifyCodeServiceImpl implements SendVerifyCodeService {

    private final SmsAdapter smsAdapter;
    private final VerifyCodeRepository verifyCodeRepository;
    private final RandomUtil randomUtil;
    private final SmsVerifyProperties smsVerifyProperties;

    @Override
    @Transactional
    public void execute(SendVerifyCodeRequest request) {
        if (verifyCodeRepository.existsById(request.phoneNumber())) {
            throw new AlreadyVerifyCodeExistsException();
        }

        String code = randomUtil.createRandomCode(6);

        verifyCodeRepository.save(VerifyCode.builder()
                .phoneNumber(request.phoneNumber())
                .code(code)
                .ttl(smsVerifyProperties.getVerifyCodeTtl())
                .build());

        smsAdapter.sendSms(request.phoneNumber(), code);
    }
}