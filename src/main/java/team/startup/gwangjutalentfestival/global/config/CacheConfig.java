package team.startup.gwangjutalentfestival.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 인메모리 캐시 설정.
 * <p>팀 랭킹, 좌석 전체/구역별 조회에 대한 캐시를 구성한다.</p>
 * <p>캐시 매니저는 {@link TransactionAwareCacheManagerProxy}로 감싸므로,
 * put/evict/clear가 트랜잭션 커밋 후에 적용되고 롤백 시에는 적용되지 않는다.
 * {@code @Transactional}과 캐시 애노테이션이 같은 메서드에 붙은 경우
 * 커밋 전에 캐시가 비워져 옛 스냅샷이 다시 캐싱되는 문제를 막는다.</p>
 */
@EnableCaching
@Configuration
public class CacheConfig {

    public static final String TEAM_RANKING  = "team:ranking";
    public static final String SEATS_ALL     = "seats:all";
    public static final String SEATS_SECTION = "seats:section";

    /**
     * 트랜잭션을 인식하는 Caffeine 캐시 매니저를 생성한다.
     *
     * @return 설정된 {@link CacheManager} 빈
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                build(TEAM_RANKING,  5,  200),
                build(SEATS_ALL,     1,  500),
                build(SEATS_SECTION, 1, 1000)
        ));
        // 빈으로 노출되는 것은 프록시뿐이라 Spring이 내부 캐시 매니저의
        // InitializingBean 콜백을 호출해주지 않는다. 직접 초기화해야 getCache()가 동작한다.
        cacheManager.afterPropertiesSet();
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }

    private CaffeineCache build(String name, int ttlMinutes, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build());
    }
}
