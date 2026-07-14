package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyAbortRequest;
import team.startup.gwangjutalentfestival.domain.apply.service.AbortApplyUploadService;
import team.startup.gwangjutalentfestival.domain.apply.util.ApplyVideoKey;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

@Service
@RequiredArgsConstructor
public class AbortApplyUploadServiceImpl implements AbortApplyUploadService {

    private final AwsS3Adapter awsS3Adapter;

    @Override
    public void execute(ApplyAbortRequest request) {
        if (request == null || ApplyVideoKey.isInvalid(request.key())
                || request.uploadId() == null || request.uploadId().isBlank()) {
            throw new InvalidVideoFileException();
        }
        awsS3Adapter.abortMultipartUpload(request.key(), request.uploadId());
    }
}
