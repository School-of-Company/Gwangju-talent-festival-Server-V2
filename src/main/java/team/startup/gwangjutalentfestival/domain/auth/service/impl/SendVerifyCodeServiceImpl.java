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

/**
 * {@link SendVerifyCodeService} 구현체.
 * <p>발송 횟수를 검증하고 6자리 인증번호를 생성하여 Redis에 저장한 뒤 SMS 발송 이벤트를 발행합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class SendVerifyCodeServiceImpl implements SendVerifyCodeService {

    private final VerifyCodeRepository verifyCodeRepository;
    private final RandomUtil randomUtil;
    private final SmsVerifyProperties smsVerifyProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 발송 횟수를 검증한 뒤 인증번호를 생성하고 SMS 발송 이벤트를 발행합니다.
     *
     * @param request 인증번호를 발송할 휴대폰 번호 요청 정보
     * @throws AlreadyVerifyCodeExistsException 이미 인증번호가 존재하거나 최대 발송 횟수를 초과한 경우
     */
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

    /**
     * 해당 휴대폰 번호의 인증번호 발송 횟수를 검증합니다.
     * <p>최대 허용 횟수를 초과하면 {@link AlreadyVerifyCodeExistsException}을 발생시킵니다.</p>
     *
     * @param phoneNumber 발송 횟수를 확인할 휴대폰 번호
     * @throws AlreadyVerifyCodeExistsException 최대 발송 횟수를 초과한 경우
     */
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