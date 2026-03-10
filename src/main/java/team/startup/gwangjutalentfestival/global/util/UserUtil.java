package team.startup.gwangjutalentfestival.global.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

@Component
public class UserUtil {
    public static Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUserId();
    }
}