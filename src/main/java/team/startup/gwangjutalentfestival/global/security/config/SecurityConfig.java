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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] PUBLIC_URLS = {
            "/auth/**",
            "/actuator/**",
            "/excel/**",
            "/vote/{teamId}",
            "/error"
    };

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
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // 예매
                        .requestMatchers(HttpMethod.POST, "/seat").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.POST, "/seat/ban").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat/ban").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/seat/myself").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.GET, "/seat").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/all").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/changes").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name(), Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.GET, "/seat/myself/performer").hasAuthority(Role.PERFORMER.name())
                        .requestMatchers(HttpMethod.DELETE, "/seat/performer").hasAuthority(Role.PERFORMER.name())
                        // 공연 팀
                        .requestMatchers(HttpMethod.GET, "/team").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.GET, "/team/ranking").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/team").hasAuthority(Role.USER.name())
                        .requestMatchers(HttpMethod.PATCH, "/team/{teamId}").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/team/{teamId}").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        // 현장 투표
                        .requestMatchers(HttpMethod.POST, "/vote").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.GET, "/vote/{teamId}/current").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/vote/{teamId}").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/vote/{teamId}").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/vote/{teamId}/extract").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/vote/{teamId}/invite").hasAuthority(Role.ADMIN.name())
                        // 전문가 심사
                        .requestMatchers(HttpMethod.PATCH, "/judge/{team_id}").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/judge").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/judge/{team_id}").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/judge/changes").hasAuthority(Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
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
