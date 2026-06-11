package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.exception.ApplyNotFoundException;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetApplyVideoUrlServiceImplTest {

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private AwsS3Adapter awsS3Adapter;

    @InjectMocks
    private GetApplyVideoUrlServiceImpl getApplyVideoUrlService;

    @Test
    void 신청_ID로_조회하면_새_Presigned_URL을_반환한다() {
        ApplyEntity apply = ApplyEntity.builder()
                .id(1L)
                .videoKey("videos/test-key.mp4")
                .originalFilename("공연영상.mp4")
                .build();
        given(applyRepository.findById(1L)).willReturn(Optional.of(apply));
        given(awsS3Adapter.generateVideoDownloadUrl("videos/test-key.mp4", "공연영상.mp4"))
                .willReturn("https://s3.example.com/videos/test-key.mp4?X-Amz-Signature=fresh");

        ApplyVideoUrlResponse response = getApplyVideoUrlService.execute(1L);

        assertThat(response.videoUrl()).isEqualTo("https://s3.example.com/videos/test-key.mp4?X-Amz-Signature=fresh");
    }

    @Test
    void 존재하지_않는_신청이면_ApplyNotFoundException이_발생한다() {
        given(applyRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getApplyVideoUrlService.execute(999L))
                .isInstanceOf(ApplyNotFoundException.class);
    }
}
