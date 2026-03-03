package team.startup.gwangjutalentfestival.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetails loadUserByUsername(Long userId) {
        return userRepository.findById(userId)
                .map(CustomUserDetails::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
