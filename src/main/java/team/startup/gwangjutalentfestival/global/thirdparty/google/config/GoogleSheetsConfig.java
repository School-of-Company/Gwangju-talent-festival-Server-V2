package team.startup.gwangjutalentfestival.global.thirdparty.google.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleSheetsProperties;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableConfigurationProperties(GoogleSheetsProperties.class)
public class GoogleSheetsConfig {

    @Bean
    public Sheets sheets(GoogleSheetsProperties properties) throws Exception {
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
    }
}
