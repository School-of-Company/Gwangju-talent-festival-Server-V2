package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyCompleteRequest;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.request.ApplyPartInput;
import team.startup.gwangjutalentfestival.domain.apply.presentation.data.response.ApplyResponse;
import team.startup.gwangjutalentfestival.domain.apply.repository.ApplyRepository;
import team.startup.gwangjutalentfestival.domain.apply.service.ApplyService;
import team.startup.gwangjutalentfestival.domain.apply.util.ApplyVideoKey;
import team.startup.gwangjutalentfestival.global.s3.adapter.AwsS3Adapter;
import team.startup.gwangjutalentfestival.global.s3.exception.InvalidVideoFileException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private static final int MP4_HEADER_LENGTH = 12;
    private static final String MP4_EXTENSION = ".mp4";
    private static final String DEFAULT_FILENAME = "video.mp4";
    private static final int MAX_FILENAME_LENGTH = 255;

    private final AwsS3Adapter awsS3Adapter;
    private final ApplyRepository applyRepository;

    @Override
    @Transactional
    public ApplyResponse execute(ApplyCompleteRequest request) {
        validateRequest(request);

        awsS3Adapter.completeMultipartUpload(request.key(), request.uploadId(), toCompletedParts(request.parts()));
        try {
            validateUploadedMp4(request.key());
        } catch (InvalidVideoFileException e) {
            // 검증 실패 시 병합 완료된 S3 객체를 정리해 불필요한 스토리지 비용을 막는다.
            awsS3Adapter.deleteObject(request.key());
            throw e;
        }

        ApplyEntity apply = applyRepository.save(
                ApplyEntity.builder()
                        .videoKey(request.key())
                        .originalFilename(resolveFilename(request.filename()))
                        .build()
        );
        return new ApplyResponse(apply.getId());
    }

    private void validateRequest(ApplyCompleteRequest request) {
        if (request == null || ApplyVideoKey.isInvalid(request.key())
                || request.uploadId() == null || request.uploadId().isBlank()
                || request.parts() == null || request.parts().isEmpty()
                || hasInvalidPart(request.parts())) {
            throw new InvalidVideoFileException();
        }
    }

    private boolean hasInvalidPart(List<ApplyPartInput> parts) {
        if (parts.stream().anyMatch(p -> p == null || p.etag() == null || p.etag().isBlank())) {
            return true;
        }
        long distinctPartNumbers = parts.stream().map(ApplyPartInput::partNumber).distinct().count();
        return distinctPartNumbers != parts.size();
    }

    private List<CompletedPart> toCompletedParts(List<ApplyPartInput> parts) {
        return parts.stream()
                .sorted(Comparator.comparingInt(ApplyPartInput::partNumber))
                .map(p -> CompletedPart.builder().partNumber(p.partNumber()).eTag(p.etag()).build())
                .toList();
    }

    private String resolveFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return DEFAULT_FILENAME;
        }
        int dotIndex = filename.lastIndexOf('.');
        String base = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        int maxBaseLength = MAX_FILENAME_LENGTH - MP4_EXTENSION.length();
        if (base.length() > maxBaseLength) {
            base = base.substring(0, maxBaseLength);
        }
        return base + MP4_EXTENSION;
    }

    private void validateUploadedMp4(String key) {
        byte[] header = awsS3Adapter.readObjectHead(key, MP4_HEADER_LENGTH);
        if (header.length < MP4_HEADER_LENGTH || !isMp4Header(header)) {
            throw new InvalidVideoFileException();
        }
    }

    private boolean isMp4Header(byte[] header) {
        // ftyp box: offset 4~7 == 0x66 0x74 0x79 0x70
        return header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70;
    }
}
