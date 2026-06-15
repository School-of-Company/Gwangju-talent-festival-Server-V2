package team.startup.gwangjutalentfestival.global.s3.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private String endpoint;
}
