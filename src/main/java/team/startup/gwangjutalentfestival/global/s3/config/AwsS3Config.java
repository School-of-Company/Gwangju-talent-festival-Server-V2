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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

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
        if (StringUtils.hasText(awsS3Properties.getEndpoint())) {
            builder.endpointOverride(URI.create(awsS3Properties.getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider awsCredentialsProvider) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider);
        if (StringUtils.hasText(awsS3Properties.getEndpoint())) {
            builder.endpointOverride(URI.create(awsS3Properties.getEndpoint()));
        }
        return builder.build();
    }
}
