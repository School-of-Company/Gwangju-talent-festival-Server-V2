package team.startup.gwangjutalentfestival.global.s3.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;
import team.startup.gwangjutalentfestival.global.s3.exception.S3MultipartException;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AwsS3Adapter {

    private static final long PRESIGNED_URL_DURATION_MINUTES = 10;
    private static final String VIDEO_CONTENT_TYPE = "video/mp4";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;

    /**
     * 멀티파트 업로드를 시작하고 uploadId를 반환한다.
     * 객체 Content-Type을 {@code video/mp4}로 고정한다.
     *
     * @param key S3 객체 key
     * @return 멀티파트 업로드 식별자(uploadId)
     * @throws S3MultipartException 업로드 시작에 실패한 경우
     */
    public String createMultipartUpload(String key) {
        try {
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .contentType(VIDEO_CONTENT_TYPE)
                            .build());
            return response.uploadId();
        } catch (SdkException e) {
            throw new S3MultipartException();
        }
    }

    /**
     * 특정 파트 번호에 대한 업로드용 Presigned URL을 발급한다.
     * 클라이언트는 이 URL로 해당 파트를 S3에 직접 PUT 업로드한다.
     *
     * @param key        S3 객체 key
     * @param uploadId   멀티파트 업로드 식별자
     * @param partNumber 파트 번호(1부터 시작)
     * @return 10분간 유효한 파트 업로드용 Presigned URL
     */
    public String generatePartUploadUrl(String key, String uploadId, int partNumber) {
        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(r -> r
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_DURATION_MINUTES))
                .uploadPartRequest(upr -> upr
                        .bucket(awsS3Properties.getBucket())
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)));
        return presigned.url().toString();
    }

    /**
     * 업로드된 파트들을 합쳐 멀티파트 업로드를 완료한다.
     *
     * @param key      S3 객체 key
     * @param uploadId 멀티파트 업로드 식별자
     * @param parts    완료된 파트 목록(파트 번호와 ETag)
     * @throws S3MultipartException 완료 처리에 실패한 경우
     */
    public void completeMultipartUpload(String key, String uploadId, List<CompletedPart> parts) {
        try {
            s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .uploadId(uploadId)
                            .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                            .build());
        } catch (SdkException e) {
            throw new S3MultipartException();
        }
    }

    /**
     * 진행 중인 멀티파트 업로드를 취소하고 업로드된 파트를 정리한다.
     *
     * @param key      S3 객체 key
     * @param uploadId 멀티파트 업로드 식별자
     * @throws S3MultipartException 취소 처리에 실패한 경우
     */
    public void abortMultipartUpload(String key, String uploadId) {
        try {
            s3Client.abortMultipartUpload(
                    AbortMultipartUploadRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .uploadId(uploadId)
                            .build());
        } catch (SdkException e) {
            throw new S3MultipartException();
        }
    }

    /**
     * S3 객체의 앞부분 바이트만 Range 요청으로 읽어 반환한다.
     * 대용량 영상 전체를 받지 않고 파일 시그니처 검증에 필요한 만큼만 읽기 위해 사용한다.
     *
     * @param key    S3 객체 key
     * @param length 읽어올 바이트 수
     * @return 객체 앞부분 바이트 배열
     * @throws InvalidVideoFileException 객체가 존재하지 않거나 읽기에 실패한 경우
     */
    public byte[] readObjectHead(String key, int length) {
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(awsS3Properties.getBucket())
                            .key(key)
                            .range("bytes=0-" + (length - 1))
                            .build());
            return objectBytes.asByteArray();
        } catch (SdkException e) {
            throw new InvalidVideoFileException();
        }
    }

    /**
     * 객체 key로 다운로드용 Presigned URL을 발급한다.
     * 서버 로컬 서명 연산이므로 호출마다 새로 발급해도 비용이 발생하지 않는다.
     *
     * @param key      S3 객체 key
     * @param filename 다운로드 시 사용할 원본 파일명
     * @return 10분간 유효한 Presigned URL
     */
    public String generateVideoDownloadUrl(String key, String filename) {
        String encodedFilename = UriUtils.encode(filename, StandardCharsets.UTF_8);
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r -> r
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_DURATION_MINUTES))
                .getObjectRequest(gor -> gor
                        .bucket(awsS3Properties.getBucket())
                        .key(key)
                        .responseContentDisposition("attachment; filename*=UTF-8''" + encodedFilename)));
        return presigned.url().toString();
    }
}
