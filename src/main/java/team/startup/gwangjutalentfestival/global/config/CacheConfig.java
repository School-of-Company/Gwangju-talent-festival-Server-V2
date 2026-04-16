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

@EnableCaching
@Configuration
public class CacheConfig {

    public static final String TEAM_ALL      = "team:all";
    public static final String TEAM_RANKING  = "team:ranking";
    public static final String SEATS_ALL     = "seats:all";
    public static final String SEATS_SECTION = "seats:section";

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