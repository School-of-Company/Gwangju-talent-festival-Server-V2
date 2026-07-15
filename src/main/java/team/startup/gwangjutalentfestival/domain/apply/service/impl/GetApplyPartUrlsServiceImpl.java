package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartUrlsRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyPartUrl;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyPartUrlsResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.GetApplyPartUrlsService;
import team.startup.gwangjutalentfestival.domain.apply.util.ApplyVideoKey;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetApplyPartUrlsServiceImpl implements GetApplyPartUrlsService {

    private static final int MAX_PART_COUNT = 10000; // S3 멀티파트 최대 파트 수

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public ApplyPartUrlsResponse execute(ApplyPartUrlsRequest request) {
        if (request == null || ApplyVideoKey.isInvalid(request.key())
                || request.uploadId() == null || request.uploadId().isBlank()
                || request.partCount() < 1 || request.partCount() > MAX_PART_COUNT) {
            throw new InvalidVideoFileException();
        }

        List<ApplyPartUrl> parts = new ArrayList<>(request.partCount());
        for (int partNumber = 1; partNumber <= request.partCount(); partNumber++) {
            String url = awsS3Adapter.generatePartUploadUrl(request.key(), request.uploadId(), partNumber);
            parts.add(new ApplyPartUrl(partNumber, url));
        }
        return new ApplyPartUrlsResponse(parts);
    }
}
