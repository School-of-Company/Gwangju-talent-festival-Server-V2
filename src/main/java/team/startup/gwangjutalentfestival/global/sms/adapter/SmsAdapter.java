package team.startup.gwangjutalentfestival.global.sms.adapter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.global.sms.exception.SmsEmptyResponseException;
import team.startup.gwangjutalentfestival.global.sms.exception.SmsSendFailedException;
import team.startup.gwangjutalentfestival.global.sms.properties.SolapiProperties;

/**
 * Solapi(nurigo SDK)를 이용한 SMS 전송 어댑터.
 * <p>애플리케이션 시작 시 {@code DefaultMessageService}를 초기화하고, 인증번호 SMS 발송을 처리한다.</p>
 */
@Component
@RequiredArgsConstructor
public class SmsAdapter {

    private static final String VERIFY_CODE_MESSAGE_FORMAT = "[광탈페] 인증번호 [%s]를 입력해주세요.";

    private final SolapiProperties solapiProperties;
    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                solapiProperties.getApiKey(),
                solapiProperties.getApiSecret(),
                solapiProperties.getUrl()
        );
    }

    /**
     * 지정된 수신 번호로 인증번호 SMS를 발송한다.
     *
     * @param to   수신자 전화번호
     * @param code 전송할 인증 코드
     * @throws SmsSendFailedException     SMS 전송에 실패한 경우
     * @throws SmsEmptyResponseException  SMS 응답이 없는 경우
     */
    public void sendSms(String to, String code) {
        Message message = new Message();
        message.setFrom(solapiProperties.getSmsPhoneNumber());
        message.setTo(to);
        message.setText(String.format(VERIFY_CODE_MESSAGE_FORMAT, code));

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException | NurigoUnknownException e) {
            throw new SmsSendFailedException();
        } catch (NurigoEmptyResponseException e) {
            throw new SmsEmptyResponseException();
        }
    }
}
