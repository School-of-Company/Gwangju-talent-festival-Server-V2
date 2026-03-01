package team.startup.gwangjutalentfestival.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.user.exception.NotFoundMemberException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(CustomUserDetails::from)
                .orElseThrow(NotFoundMemberException::new);
    }
}
