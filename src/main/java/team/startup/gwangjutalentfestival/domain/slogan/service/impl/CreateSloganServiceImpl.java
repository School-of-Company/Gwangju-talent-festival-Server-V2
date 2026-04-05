package team.startup.gwangjutalentfestival.domain.slogan.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.exception.SloganSubmissionPeriodException;
import team.startup.gwangjutalentfestival.domain.slogan.properties.SloganSubmissionProperties;
import team.startup.gwangjutalentfestival.domain.slogan.repository.SloganRepository;
import team.startup.gwangjutalentfestival.domain.slogan.service.CreateSloganService;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateSloganServiceImpl implements CreateSloganService {

    private final SloganRepository sloganRepository;
    private final SloganSubmissionProperties sloganSubmissionProperties;

    @Override
    @Transactional
    public void execute(CreateSloganRequest request) {
        validateSloganSubmissionPeriod();
        validateDuplicatePhoneNumber(request.phoneNumber());

        SloganEntity slogan = SloganEntity.builder()
                .slogan(request.slogan())
                .description(request.description())
                .school(request.school())
                .grade(request.grade())
                .classNum(request.classNum())
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .sheetSyncStatus(SheetSyncStatus.PENDING)
                .nextRetryAt(LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID))
                .build();

        sloganRepository.save(slogan);
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
