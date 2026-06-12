package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.exception.ApplyNotFoundException;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyVideoUrlResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyVideoUrlService;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

@Service
@RequiredArgsConstructor
public class GetApplyVideoUrlServiceImpl implements GetApplyVideoUrlService {

    private final ApplyRepository applyRepository;
    private final AwsS3Adapter awsS3Adapter;

    @Override
    @Transactional(readOnly = true)
    public ApplyVideoUrlResponse execute(Long applyId) {
        ApplyEntity apply = applyRepository.findById(applyId)
                .orElseThrow(ApplyNotFoundException::new);
        String url = awsS3Adapter.generateVideoDownloadUrl(apply.getVideoKey(), apply.getOriginalFilename());
        return new ApplyVideoUrlResponse(url);
    }
}
