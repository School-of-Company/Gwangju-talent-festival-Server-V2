package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadInitiateResponse;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InitiateApplyUploadServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private InitiateApplyUploadServiceImpl initiateApplyUploadService;

    @Test
    void videos_경로_key를_생성하고_uploadId를_발급한다() {
        given(awsS3Adapter.createMultipartUpload(startsWith("videos/"))).willReturn("upload-id-123");

        ApplyUploadInitiateResponse response = initiateApplyUploadService.execute();

        assertThat(response.key()).startsWith("videos/").endsWith(".mp4");
        assertThat(response.uploadId()).isEqualTo("upload-id-123");
    }

    @Test
    void 시작할_때마다_서로_다른_key를_생성한다() {
        given(awsS3Adapter.createMultipartUpload(startsWith("videos/"))).willReturn("upload-id");

        String first = initiateApplyUploadService.execute().key();
        String second = initiateApplyUploadService.execute().key();

        assertThat(first).isNotEqualTo(second);
    }
}
