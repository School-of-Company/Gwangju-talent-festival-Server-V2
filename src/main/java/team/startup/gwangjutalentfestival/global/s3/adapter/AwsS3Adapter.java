package team.startup.gwangjutalentfestival.global.s3.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import team.startup.gwangjutalentfestival.global.s3.exception.S3UploadFailedException;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AwsS3Adapter {

    private static final long PRESIGNED_URL_DURATION_DAYS = 7;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;

    public String uploadVideo(MultipartFile file) {
        String key = "videos/" + UUID.randomUUID() + ".mp4";
        String rawFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "video.mp4";
        String safeFilename = rawFilename.replaceAll("[\"\\r\\n]", "_");
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .contentType("video/mp4")
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException | SdkException e) {
            throw new S3UploadFailedException();
        }
        return generatePresignedUrl(key, safeFilename);
    }

    private String generatePresignedUrl(String key, String filename) {
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r -> r
                .signatureDuration(Duration.ofDays(PRESIGNED_URL_DURATION_DAYS))
                .getObjectRequest(gor -> gor
                        .bucket(awsS3Properties.getBucket())
                        .key(key)
                        .responseContentDisposition("attachment; filename=\"" + filename + "\"")));
        return presigned.url().toString();
    }
}
