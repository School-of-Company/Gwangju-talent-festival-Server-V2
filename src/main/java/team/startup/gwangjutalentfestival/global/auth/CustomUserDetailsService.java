package team.startup.gwangjutalentfestival.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

/**
 * 사용자 ID 기반으로 {@link CustomUserDetails}를 로드하는 서비스.
 * <p>JWT 인증 흐름에서 데이터베이스로부터 사용자 정보를 조회할 때 사용된다.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자 정보를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 객체
     * @throws team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException 사용자가 존재하지 않을 경우
     */
    public CustomUserDetails loadUserByUsername(Long userId) {
        return userRepository.findById(userId)
                .map(CustomUserDetails::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
