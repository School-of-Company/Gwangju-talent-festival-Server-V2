package team.startup.gwangjutalentfestival.global.thirdparty.google.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsInitException;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleSheetsProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(GoogleSheetsProperties.class)
public class GoogleSheetsConfig {

    @Bean
    public Sheets sheets(GoogleSheetsProperties properties) {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(
                            new ByteArrayInputStream(
                                    properties.accountCredential().getBytes(StandardCharsets.UTF_8)
                            )
                    )
                    .createScoped(List.of(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName("gwangju-talent-festival-Slogan")
                    .build();
        } catch (IOException e) {
            log.error("Google Sheets 인증 파일 읽기 실패 - message: {}", e.getMessage());
            throw new GoogleSheetsInitException();
        } catch (GeneralSecurityException e) {
            log.error("Google Sheets TLS 초기화 실패 - message: {}", e.getMessage());
            throw new GoogleSheetsInitException();
        } catch (Exception e) {
            log.error("Google Sheets 초기화 중 예기치 못한 오류 발생 - message: {}", e.getMessage());
            throw new GoogleSheetsInitException();
        }
    }
}