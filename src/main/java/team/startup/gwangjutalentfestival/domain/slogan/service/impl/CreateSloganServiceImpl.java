package team.startup.gwangjutalentfestival.domain.slogan.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.auth.exception.DuplicatePhoneNumberException;
import team.startup.gwangjutalentfestival.domain.slogan.dto.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.repository.SloganRepository;
import team.startup.gwangjutalentfestival.domain.slogan.service.CreateSloganService;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleSheetsAdapter;

@Service
@RequiredArgsConstructor
public class CreateSloganServiceImpl implements CreateSloganService {

    private final SloganRepository sloganRepository;
    private final GoogleSheetsAdapter googleSheetsAdapter;

    @Override
    @Transactional
    public void execute(CreateSloganRequest request) {
        validateDuplicatePhoneNumber(request.phoneNumber());

        SloganEntity slogan = SloganEntity.builder()
                .slogan(request.slogan())
                .description(request.description())
                .school(request.school())
                .grade(request.grade())
                .classNum(request.classNum())
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .build();

        sloganRepository.save(slogan);
        googleSheetsAdapter.appendSlogan(request);
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (sloganRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException();
        }
    }
}
