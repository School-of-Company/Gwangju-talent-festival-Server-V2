package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartUrlsRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyPartUrlsResponse;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetApplyPartUrlsServiceImplTest {

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private GetApplyPartUrlsServiceImpl getApplyPartUrlsService;

    @Test
    void partCount만큼_파트_업로드_URL을_발급한다() {
        given(awsS3Adapter.generatePartUploadUrl(anyString(), anyString(), anyInt()))
                .willReturn("https://s3.example.com/part");
        ApplyPartUrlsRequest req = new ApplyPartUrlsRequest("videos/key.mp4", "upload-id", 3);

        ApplyPartUrlsResponse response = getApplyPartUrlsService.execute(req);

        assertThat(response.parts()).hasSize(3);
        assertThat(response.parts()).extracting("partNumber").containsExactly(1, 2, 3);
    }

    @Test
    void key가_규격을_벗어나면_InvalidVideoFileException이_발생한다() {
        ApplyPartUrlsRequest req = new ApplyPartUrlsRequest("uploads/evil.mp4", "upload-id", 1);

        assertThatThrownBy(() -> getApplyPartUrlsService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void partCount가_0이하이면_InvalidVideoFileException이_발생한다() {
        ApplyPartUrlsRequest req = new ApplyPartUrlsRequest("videos/key.mp4", "upload-id", 0);

        assertThatThrownBy(() -> getApplyPartUrlsService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }

    @Test
    void partCount가_상한을_초과하면_InvalidVideoFileException이_발생한다() {
        ApplyPartUrlsRequest req = new ApplyPartUrlsRequest("videos/key.mp4", "upload-id", 10001);

        assertThatThrownBy(() -> getApplyPartUrlsService.execute(req))
                .isInstanceOf(InvalidVideoFileException.class);
    }
}
