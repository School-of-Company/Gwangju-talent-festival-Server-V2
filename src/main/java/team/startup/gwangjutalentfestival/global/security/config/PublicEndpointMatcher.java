package team.startup.gwangjutalentfestival.global.security.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public final class PublicEndpointMatcher {

    private static final List<RequestMatcher> PUBLIC_ENDPOINTS = List.of(
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/auth(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/excel(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/actuator/health(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/actuator/prometheus(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/vote/[^/]+")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/error")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/swagger-ui(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(withOptionalQuery("^/v3/api-docs(?:/.*)?")),
            RegexRequestMatcher.regexMatcher(HttpMethod.GET, withOptionalQuery("^/team")),
            RegexRequestMatcher.regexMatcher(HttpMethod.POST, withOptionalQuery("^/apply")),
            RegexRequestMatcher.regexMatcher(HttpMethod.POST, withOptionalQuery("^/apply/upload/(?:initiate|part-urls|abort)")),
            RegexRequestMatcher.regexMatcher(HttpMethod.GET, withOptionalQuery("^/apply/[^/]+/video")),
            RegexRequestMatcher.regexMatcher(HttpMethod.POST, withOptionalQuery("^/slogan"))
    );

    private PublicEndpointMatcher() {
    }

    public static RequestMatcher matcher() {
        return new OrRequestMatcher(PUBLIC_ENDPOINTS);
    }

    public static RequestMatcher[] matchers() {
        return PUBLIC_ENDPOINTS.toArray(RequestMatcher[]::new);
    }

    private static String withOptionalQuery(String pathPattern) {
        return pathPattern + "(?:\\?.*)?$";
    }
}
