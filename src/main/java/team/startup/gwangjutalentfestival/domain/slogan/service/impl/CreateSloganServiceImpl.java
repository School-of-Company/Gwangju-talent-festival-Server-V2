package team.startup.gwangjutalentfestival.domain.slogan.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SchoolStatus;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;
import team.startup.gwangjutalentfestival.domain.slogan.exception.SloganRequiredFieldMissingException;
import team.startup.gwangjutalentfestival.domain.slogan.exception.SloganSubmissionPeriodException;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.domain.slogan.properties.SloganSubmissionProperties;
import team.startup.gwangjutalentfestival.domain.slogan.repository.SloganRepository;
import team.startup.gwangjutalentfestival.domain.slogan.service.CreateSloganService;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;

import java.time.LocalDateTime;

/**
 * {@link CreateSloganService} 구현체.
 * 접수 기간 유효성 및 전화번호 중복을 검증한 후 슬로건을 저장한다.
 * schoolStatus에 따라 수집 필드를 분기하며, 저장된 슬로건은 Google Sheets 동기화 대기 상태({@link SheetSyncStatus#PENDING})로 설정된다.
 */
@Service
@RequiredArgsConstructor
public class CreateSloganServiceImpl implements CreateSloganService {

    private final SloganRepository sloganRepository;
    private final SloganSubmissionProperties sloganSubmissionProperties;

    /**
     * 슬로건을 등록한다.
     * 접수 기간이 아니면 {@link SloganSubmissionPeriodException},
     * 동일 전화번호가 이미 존재하면 {@link DuplicatePhoneNumberException},
     * schoolStatus별 필수 필드가 누락되면 {@link SloganRequiredFieldMissingException}이 발생한다.
     *
     * @param request 슬로건 등록 요청 데이터
     */
    @Override
    @Transactional
    public void execute(CreateSloganRequest request) {
        validateSloganSubmissionPeriod();
        validateDuplicatePhoneNumber(request.phoneNumber());

        SloganEntity slogan = request.schoolStatus() == SchoolStatus.OUT_OF_SCHOOL
                ? buildOutOfSchoolSlogan(request)
                : buildEnrolledSlogan(request);

        sloganRepository.save(slogan);
    }

    private SloganEntity buildOutOfSchoolSlogan(CreateSloganRequest request) {
        if (request.birthDate() == null) {
            throw new SloganRequiredFieldMissingException();
        }

        return SloganEntity.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .slogan(request.slogan())
                .description(request.description())
                .birthDate(request.birthDate())
                .schoolStatus(SchoolStatus.OUT_OF_SCHOOL)
                .sheetSyncStatus(SheetSyncStatus.PENDING)
                .nextRetryAt(LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID))
                .build();
    }

    private SloganEntity buildEnrolledSlogan(CreateSloganRequest request) {
        if (isBlank(request.slogan()) || isBlank(request.description())
                || isBlank(request.school()) || request.grade() == null || request.classNum() == null) {
            throw new SloganRequiredFieldMissingException();
        }

        return SloganEntity.builder()
                .slogan(request.slogan())
                .description(request.description())
                .school(request.school())
                .grade(request.grade())
                .classNum(request.classNum())
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .schoolStatus(SchoolStatus.ENROLLED)
                .sheetSyncStatus(SheetSyncStatus.PENDING)
                .nextRetryAt(LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID))
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (sloganRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException();
        }
    }

    private void validateSloganSubmissionPeriod() {
        LocalDateTime now = LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID);

        if (now.isBefore(sloganSubmissionProperties.startAt()) ||
                now.isAfter(sloganSubmissionProperties.endAt())) {
            throw new SloganSubmissionPeriodException();
        }
    }
}