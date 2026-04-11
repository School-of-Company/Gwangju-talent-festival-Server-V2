package team.startup.gwangjutalentfestival.domain.auth.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.startup.gwangjutalentfestival.domain.auth.event.VerifyCodeCreatedEvent;
import team.startup.gwangjutalentfestival.domain.auth.repository.VerifyCodeRepository;
import team.startup.gwangjutalentfestival.global.sms.adapter.SmsAdapter;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyCodeEventListener {

    private final SmsAdapter smsAdapter;
    private final VerifyCodeRepository verifyCodeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SmsVerifyProperties smsVerifyProperties;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VerifyCodeCreatedEvent event) {
        try {
            smsAdapter.sendSms(event.phoneNumber(), event.code());
        } catch (RuntimeException e) {
            log.error("[SMS 전송 실패] phoneNumber={}, message={}", event.phoneNumber(), e.getMessage(), e);
            verifyCodeRepository.deleteById(event.phoneNumber());
            String key = smsVerifyProperties.getVerifyCountKeyPrefix() + event.phoneNumber();
            Long count = redisTemplate.opsForValue().decrement(key);
            if (count != null && count < 0) {
                redisTemplate.delete(key);
            }
        }
    }
}
