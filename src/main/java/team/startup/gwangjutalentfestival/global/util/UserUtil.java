package team.startup.gwangjutalentfestival.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

/**
 * 현재 인증된 사용자 정보를 조회하는 유틸리티.
 * <p>{@link org.springframework.security.core.context.SecurityContext}에서 사용자 ID와 역할을 추출하거나,
 * DB에서 사용자 엔티티를 조회하는 편의 메서드를 제공한다.</p>
 */
@Component
@RequiredArgsConstructor
public class UserUtil {

    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자의 ID를 반환한다.
     *
     * @return 현재 사용자 ID
     */
    public static Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUserId();
    }

    /**
     * 현재 인증된 사용자의 역할을 반환한다.
     *
     * @return 현재 사용자 역할
     */
    public static Role getCurrentUserRole() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getRole();
    }

    /**
     * 현재 인증된 사용자의 엔티티를 DB에서 조회한다.
     *
     * @return 현재 사용자 엔티티
     * @throws team.startup.gwangjutalentfestival.domain.user.exception.UserNotFoundException 사용자가 존재하지 않을 경우
     */
    public UserEntity getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * 현재 인증된 사용자의 프록시 참조(Lazy 로딩)를 반환한다.
     * <p>실제 DB 조회 없이 참조만 필요한 경우 사용한다.</p>
     *
     * @return 현재 사용자 엔티티 프록시 참조
     */
    public UserEntity getCurrentUserRef() {
        return userRepository.getReferenceById(getCurrentUserId());
    }

    /**
     * 현재 인증된 사용자의 ID를 반환한다. {@link #getCurrentUserId()}의 인스턴스 메서드 래퍼.
     *
     * @return 현재 사용자 ID
     */
    public Long currentUserId() {
        return getCurrentUserId();
    }

    /**
     * 현재 인증된 사용자의 역할을 반환한다. {@link #getCurrentUserRole()}의 인스턴스 메서드 래퍼.
     *
     * @return 현재 사용자 역할
     */
    public Role currentUserRole() {
        return getCurrentUserRole();
    }
}
