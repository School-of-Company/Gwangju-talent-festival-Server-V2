package team.startup.gwangjutalentfestival.global.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@ConfigurationProperties(prefix = "cors")
@Setter
public class CorsProperties {
    private List<String> allowedOrigins;
}