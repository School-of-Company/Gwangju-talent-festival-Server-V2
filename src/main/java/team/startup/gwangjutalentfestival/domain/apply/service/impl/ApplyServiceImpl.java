package team.startup.gwangjutalentfestival.domain.apply.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private static final int MP4_HEADER_LENGTH = 12;
    private static final int MP4_VALIDATION_HEADER_LENGTH = 1024;
    private static final String MP4_EXTENSION = ".mp4";
    private static final String DEFAULT_FILENAME = "video.mp4";
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final int MIN_PART_NUMBER = 1;
    private static final int MAX_PART_NUMBER = 10000; // S3 멀티파트 최대 파트 번호

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
        if (request == null) {
            log.warn("[apply] validateRequest 실패: request가 null");
            throw new InvalidVideoFileException();
        }
        if (ApplyVideoKey.isInvalid(request.key())) {
            log.warn("[apply] validateRequest 실패: 잘못된 key={}", request.key());
            throw new InvalidVideoFileException();
        }
        if (request.uploadId() == null || request.uploadId().isBlank()) {
            log.warn("[apply] validateRequest 실패: uploadId가 null 또는 빈 값, key={}", request.key());
            throw new InvalidVideoFileException();
        }
        if (request.parts() == null || request.parts().isEmpty()) {
            log.warn("[apply] validateRequest 실패: parts가 null 또는 비어있음, key={}", request.key());
            throw new InvalidVideoFileException();
        }
        if (hasInvalidPart(request.parts())) {
            log.warn("[apply] validateRequest 실패: 유효하지 않은 part 존재, key={}, parts={}", request.key(), request.parts());
            throw new InvalidVideoFileException();
        }
    }

    private boolean hasInvalidPart(List<ApplyPartInput> parts) {
        if (parts.stream().anyMatch(p -> p == null || p.etag() == null || p.etag().isBlank()
                || p.partNumber() < MIN_PART_NUMBER || p.partNumber() > MAX_PART_NUMBER)) {
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
        // 경로 구분자(/, \) 이후의 순수 파일명만 추출해 경로 이탈을 방지한다.
        String cleanFilename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
        if (cleanFilename.isBlank()) {
            return DEFAULT_FILENAME;
        }
        int dotIndex = cleanFilename.lastIndexOf('.');
        String base = dotIndex > 0 ? cleanFilename.substring(0, dotIndex) : cleanFilename;
        int maxBaseLength = MAX_FILENAME_LENGTH - MP4_EXTENSION.length();
        if (base.length() > maxBaseLength) {
            base = base.substring(0, maxBaseLength);
        }
        return base + MP4_EXTENSION;
    }

    private void validateUploadedMp4(String key) {
        byte[] header = awsS3Adapter.readObjectHead(key, MP4_VALIDATION_HEADER_LENGTH);
        log.info("[apply] MP4 헤더 읽기 완료: key={}, headerLength={}", key, header.length);
        if (header.length < MP4_HEADER_LENGTH) {
            log.warn("[apply] validateUploadedMp4 실패: 헤더 길이 부족 (읽은 길이={}, 필요={}) key={}", header.length, MP4_HEADER_LENGTH, key);
            throw new InvalidVideoFileException();
        }
        if (!isMp4Header(header)) {
            log.warn("[apply] validateUploadedMp4 실패: ftyp box 불일치, key={}, offset4-7=[{}, {}, {}, {}]",
                    key,
                    String.format("0x%02X", header[4]),
                    String.format("0x%02X", header[5]),
                    String.format("0x%02X", header[6]),
                    String.format("0x%02X", header[7]));
            throw new InvalidVideoFileException();
        }
        log.info("[apply] MP4 헤더 검증 성공: key={}", key);
    }

    private boolean isMp4Header(byte[] header) {
        // ftyp box가 항상 offset 4에 있지 않을 수 있으므로 범위 내에서 탐색한다.
        for (int i = 4; i <= header.length - 4; i++) {
            if (header[i] == 0x66 && header[i + 1] == 0x74 && header[i + 2] == 0x79 && header[i + 3] == 0x70) {
                return true;
            }
        }
        return false;
    }
}
