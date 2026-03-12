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

@Component
@RequiredArgsConstructor
public class SmsAdapter {

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

    public void sendSms(String to, String code) {
        Message message = new Message();
        message.setFrom(solapiProperties.getSmsPhoneNumber());
        message.setTo(to);
        message.setText("[광탈페] 인증번호 [" + code + "]를 입력해주세요.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException | NurigoUnknownException e) {
            throw new SmsSendFailedException();
        } catch (NurigoEmptyResponseException e) {
            throw new SmsEmptyResponseException();
        }
    }
}
