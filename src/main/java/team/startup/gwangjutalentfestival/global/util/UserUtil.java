package team.startup.gwangjutalentfestival.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private final UserRepository userRepository;

    public static Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUserId();
    }

    public UserEntity getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(UserNotFoundException::new);
    }
}