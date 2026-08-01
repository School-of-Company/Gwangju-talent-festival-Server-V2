package team.startup.gwangjutalentfestival.global.security.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.security.filter.JwtFilter;
import team.startup.gwangjutalentfestival.global.security.handler.JwtAccessDeniedHandler;
import team.startup.gwangjutalentfestival.global.security.handler.JwtAuthenticationEntryPoint;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;

import java.util.List;

/**
 * Spring Security 보안 설정.
 * <p>JWT 필터 등록, URL별 접근 권한, CORS, CSRF, 세션 정책을 구성한다.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsProperties corsProperties;

    /**
     * HTTP 보안 필터 체인을 구성한다.
     *
     * @param http {@link HttpSecurity} 객체
     * @return 구성된 {@link SecurityFilterChain}
     * @throws Exception 보안 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(it ->
                        it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(it ->
                        it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(it ->it
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers(PublicEndpointMatcher.matchers()).permitAll()

                        // team
                        .requestMatchers("/team/**").hasAnyAuthority(Role.ADMIN.name())

                        // seat
                        .requestMatchers(HttpMethod.POST, "/seat").hasAnyAuthority(Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.POST, "/seat/ban").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat/ban").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat/performer").hasAnyAuthority(Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/myself").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/myself/performer").hasAnyAuthority(Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/all").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/changes").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())

                        // judge
                        .requestMatchers(HttpMethod.GET, "/judge/monitor/changes").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers("/judge/**").hasAnyAuthority(Role.JUDGE.name())

                        // monitoring
                        .requestMatchers("/monitoring/**").hasAnyAuthority(Role.ADMIN.name())

                        // excel
                        .requestMatchers("/excel/**").hasAnyAuthority(Role.ADMIN.name())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * CORS 설정 소스를 구성한다.
     * <p>허용 출처는 {@link CorsProperties}에서 읽어온다.</p>
     *
     * @return {@link CorsConfigurationSource} 빈
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
