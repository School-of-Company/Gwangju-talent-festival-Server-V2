package team.startup.gwangjutalentfestival.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 인메모리 캐시 설정.
 * <p>팀 목록, 팀 랭킹, 좌석 전체/구역별 조회에 대한 캐시를 구성한다.</p>
 */
@EnableCaching
@Configuration
public class CacheConfig {

    public static final String TEAM_ALL      = "team:all";
    public static final String TEAM_RANKING  = "team:ranking";
    public static final String SEATS_ALL     = "seats:all";
    public static final String SEATS_SECTION = "seats:section";

    /**
     * Caffeine 캐시 매니저를 생성한다.
     *
     * @return 설정된 {@link CacheManager} 빈
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                build(TEAM_ALL,      5,  200),
                build(TEAM_RANKING,  5,  200),
                build(SEATS_ALL,     1,  500),
                build(SEATS_SECTION, 1, 1000)
        ));
        return cacheManager;
    }

    private CaffeineCache build(String name, int ttlMinutes, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build());
    }
}