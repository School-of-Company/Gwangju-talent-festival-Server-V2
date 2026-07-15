package team.startup.gwangjutalentfestival.global.s3.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

    // R2 기본 엔드포인트는 path-style만 지원(가상 호스트 스타일은 와일드카드 인증서 미커버로 SSL 실패)
    private static final S3Configuration PATH_STYLE = S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build();

    private final AwsS3Properties awsS3Properties;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsS3Properties.getAccessKey(), awsS3Properties.getSecretKey())
        );
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider awsCredentialsProvider) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider);
        if (hasCustomEndpoint()) {
            builder.endpointOverride(customEndpoint())
                    .serviceConfiguration(PATH_STYLE);
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider awsCredentialsProvider) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider);
        if (hasCustomEndpoint()) {
            builder.endpointOverride(customEndpoint())
                    .serviceConfiguration(PATH_STYLE);
        }
        return builder.build();
    }

    private boolean hasCustomEndpoint() {
        return StringUtils.hasText(awsS3Properties.getEndpoint());
    }

    private URI customEndpoint() {
        URI uri = URI.create(awsS3Properties.getEndpoint());
        String scheme = uri.getScheme();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalStateException(
                    "aws.s3.endpoint는 http:// 또는 https:// 스키마로 시작해야 합니다: " + awsS3Properties.getEndpoint());
        }
        return uri;
    }
}
