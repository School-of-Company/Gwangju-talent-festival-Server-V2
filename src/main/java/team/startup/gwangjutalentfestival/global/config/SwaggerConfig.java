package team.startup.gwangjutalentfestival.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(springdoc-openapi) 문서 설정.
 * <p>API 문서 제목, 버전 정보와 JWT Bearer 보안 스키마를 정의한다.</p>
 */
@OpenAPIDefinition(
        info = @Info(
                title = "광주 탤런트 페스티벌 API",
                version = "2.0.0"
        )
)
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class SwaggerConfig {
}