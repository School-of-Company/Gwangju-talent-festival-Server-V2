package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadUrlResponse;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetApplyUploadUrlServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private GetApplyUploadUrlServiceImpl getApplyUploadUrlService;

    @Test
    void videos_경로의_key와_업로드_URL을_발급한다() {
        given(awsS3Adapter.generateUploadUrl(startsWith("videos/")))
                .willReturn("https://s3.example.com/upload?X-Amz-Signature=put");

        ApplyUploadUrlResponse response = getApplyUploadUrlService.execute();

        assertThat(response.key()).startsWith("videos/").endsWith(".mp4");
        assertThat(response.uploadUrl()).isEqualTo("https://s3.example.com/upload?X-Amz-Signature=put");
    }

    @Test
    void 발급할_때마다_서로_다른_key를_생성한다() {
        given(awsS3Adapter.generateUploadUrl(startsWith("videos/"))).willReturn("https://s3.example.com/upload");

        ApplyUploadUrlResponse first = getApplyUploadUrlService.execute();
        ApplyUploadUrlResponse second = getApplyUploadUrlService.execute();

        assertThat(first.key()).isNotEqualTo(second.key());
    }
}
