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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", ".mp4");
            file.transferTo(tempFile);
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .contentType("video/mp4")
                            .contentLength(tempFile.length())
                            .build(),
                    RequestBody.fromFile(tempFile)
            );
        } catch (IOException | SdkException e) {
            throw new S3UploadFailedException();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return generatePresignedUrl(key, rawFilename);
    }

    private String generatePresignedUrl(String key, String filename) {
        String encodedFilename = org.springframework.web.util.UriUtils.encode(filename, StandardCharsets.UTF_8);
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r -> r
                .signatureDuration(Duration.ofDays(PRESIGNED_URL_DURATION_DAYS))
                .getObjectRequest(gor -> gor
                        .bucket(awsS3Properties.getBucket())
                        .key(key)
                        .responseContentDisposition("attachment; filename*=UTF-8''" + encodedFilename)));
        return presigned.url().toString();
    }
}
