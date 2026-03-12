package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCodeCount;
import team.startup.gwangjutalentfestival.domain.auth.exception.ExceededVerifyCountException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeCountRepository;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.SendVerifyCodeService;
import team.startup.gwangjutalentfestival.global.sms.adapter.SmsAdapter;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;
import team.startup.gwangjutalentfestival.global.util.RandomUtil;

@Service
@RequiredArgsConstructor
public class SendVerifyCodeServiceImpl implements SendVerifyCodeService {

    private final SmsAdapter smsAdapter;
    private final VerifyCodeCountRepository verifyCodeCountRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final RandomUtil randomUtil;
    private final SmsVerifyProperties smsVerifyProperties;

    @Override
    @Transactional
    public void execute(SendVerifyCodeRequest request) {
        validateAndIncreaseCount(request.phoneNumber());

        String code = randomUtil.createRandomCode(6);

        saveVerifyCode(request.phoneNumber(), code);
        smsAdapter.sendSms(request.phoneNumber(), code);
    }

    private void saveVerifyCode(String phoneNumber, String code) {
        verifyCodeRepository.save(VerifyCode.builder()
                .phoneNumber(phoneNumber)
                .code(code)
                .ttl(smsVerifyProperties.getVerifyCodeTtl())
                .build());
    }

    private void validateAndIncreaseCount(String phoneNumber) {
        VerifyCodeCount verifyCodeCount = verifyCodeCountRepository.findById(phoneNumber)
                .orElse(VerifyCodeCount.builder()
                        .phoneNumber(phoneNumber)
                        .count(0)
                        .ttl(smsVerifyProperties.getVerifyCountTtl())
                        .build());

        if (verifyCodeCount.isExceeded(smsVerifyProperties.getMaxSendCount())) {
            throw new ExceededVerifyCountException();
        }

        verifyCodeCount.increment();
        verifyCodeCountRepository.save(verifyCodeCount);
    }
}
