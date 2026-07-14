package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyUploadInitiateResponse;
import team.startup.gwangjutalentfestival.domain.apply.service.InitiateApplyUploadService;
import team.startup.gwangjutalentfestival.domain.apply.util.ApplyVideoKey;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;

@Service
@RequiredArgsConstructor
public class InitiateApplyUploadServiceImpl implements InitiateApplyUploadService {

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public ApplyUploadInitiateResponse execute() {
        String key = ApplyVideoKey.generate();
        String uploadId = awsS3Adapter.createMultipartUpload(key);
        return new ApplyUploadInitiateResponse(key, uploadId);
    }
}
