package team.startup.gwangjutalentfestival.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.auth.service.MemberRegistrationService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.oauth.data.MemberCommand;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.oauth.exception.OAuth2AuthenticationProcessingException;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    private final UserRepository userRepository;

    @Override
    public UserEntity findOrRegister(MemberCommand command) {
        try {
            return userRepository.findByEmail(command.email())
                    .orElseGet(() -> registerMember(command));
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByEmail(command.email())
                    .orElseThrow(OAuth2AuthenticationProcessingException::new);
        }
    }

    private UserEntity registerMember(MemberCommand command) {
        return userRepository.save(UserEntity.builder()
                .email(command.email())
                .providerId(command.providerId())
                .provider(command.type())
                .role(Role.USER)
                .build());
    }
}
