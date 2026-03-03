package team.startup.gwangjutalentfestival.global.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String phoneNumber;
    private final String password;
    private final Role role;

    private CustomUserDetails(Long userId, String phoneNumber, String password, Role role) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    public static CustomUserDetails from(UserEntity user) {
        return new CustomUserDetails(user.getId(), user.getPhoneNumber(), user.getPassword(), user.getRole());
    }

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
