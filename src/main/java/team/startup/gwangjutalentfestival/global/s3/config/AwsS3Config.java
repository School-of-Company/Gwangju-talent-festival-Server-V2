package team.startup.gwangjutalentfestival.global.s3.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

    private final AwsS3Properties awsS3Properties;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsS3Properties.getAccessKey(), awsS3Properties.getSecretKey())
                ))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsS3Properties.getAccessKey(), awsS3Properties.getSecretKey())
                ))
                .build();
    }
}
