package team.startup.gwangjutalentfestival.domain.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.entity.VerifyCode;
import team.startup.gwangjutalentfestival.domain.auth.event.VerifyCodeCreatedEvent;
import team.startup.gwangjutalentfestival.domain.auth.exception.AlreadyVerifyCodeExistsException;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.request.SendVerifyCodeRequest;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.domain.auth.service.SendVerifyCodeService;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;
import team.startup.gwangjutalentfestival.global.util.RandomUtil;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SendVerifyCodeServiceImpl implements SendVerifyCodeService {

    private final VerifyCodeRepository verifyCodeRepository;
    private final RandomUtil randomUtil;
    private final SmsVerifyProperties smsVerifyProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void execute(SendVerifyCodeRequest request) {
        validateSendCount(request.phoneNumber());

        if (verifyCodeRepository.existsById(request.phoneNumber())) {
            throw new AlreadyVerifyCodeExistsException();
        }

        String code = randomUtil.createRandomCode(6);

        verifyCodeRepository.save(VerifyCode.builder()
                .phoneNumber(request.phoneNumber())
                .code(code)
                .ttl(smsVerifyProperties.getVerifyCodeTtl())
                .build());

        applicationEventPublisher.publishEvent(new VerifyCodeCreatedEvent(request.phoneNumber(), code));
    }

    private void validateSendCount(String phoneNumber) {
        String key = smsVerifyProperties.getVerifyCountKey(phoneNumber);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, smsVerifyProperties.getVerifyCountTtl(), TimeUnit.SECONDS);
        }
        if (count > smsVerifyProperties.getMaxSendCount()) {
            throw new AlreadyVerifyCodeExistsException();
        }
    }
}