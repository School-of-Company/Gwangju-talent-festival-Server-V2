package team.startup.gwangjutalentfestival.global.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security에서 사용하는 커스텀 사용자 정보 클래스.
 * <p>{@link UserEntity}나 JWT 클레임으로부터 생성되며, 사용자 ID와 역할 정보를 보유한다.</p>
 */
public class CustomUserDetails implements UserDetails {
    @Getter
    private final Long userId;
    private final String phoneNumber;
    private final String password;
    @Getter
    private final Role role;

    private CustomUserDetails(Long userId, String phoneNumber, String password, Role role) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    /**
     * {@link UserEntity}로부터 {@link CustomUserDetails}를 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 사용자 정보 객체
     */
    public static CustomUserDetails from(UserEntity user) {
        return new CustomUserDetails(user.getId(), user.getPhoneNumber(), user.getPassword(), user.getRole());
    }

    /**
     * JWT 토큰 클레임으로부터 {@link CustomUserDetails}를 생성한다.
     * <p>전화번호와 비밀번호는 null로 설정된다.</p>
     *
     * @param userId 사용자 ID
     * @param role   사용자 역할
     * @return 사용자 정보 객체
     */
    public static CustomUserDetails fromToken(Long userId, Role role) {
        return new CustomUserDetails(userId, null, null, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
