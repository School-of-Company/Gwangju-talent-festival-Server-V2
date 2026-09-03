package team.startup.gwangjutalentfestival.global.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new CacheConfig().cacheManager();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 팀_전체_조회_캐시는_등록되지_않는다() {
        assertThat(cacheManager.getCache("team:all")).isNull();
        assertThat(cacheManager.getCacheNames()).doesNotContain("team:all");
    }

    @Test
    void 등록된_캐시는_초기화되어_조회된다() {
        assertThat(cacheManager.getCache(CacheConfig.TEAM_RANKING)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.SEATS_ALL)).isNotNull();
        assertThat(cacheManager.getCache(CacheConfig.SEATS_SECTION)).isNotNull();
    }

    @Test
    void 트랜잭션이_없으면_캐시_삭제가_즉시_적용된다() {
        Cache cache = cacheManager.getCache(CacheConfig.TEAM_RANKING);
        cache.put("key", "value");

        cache.clear();

        assertThat(cache.get("key")).isNull();
    }

    @Test
    void 캐시_삭제는_트랜잭션_커밋_후에_적용된다() {
        Cache cache = cacheManager.getCache(CacheConfig.SEATS_ALL);
        cache.put("key", "value");
        TransactionSynchronizationManager.initSynchronization();

        cache.clear();

        assertThat(cache.get("key")).isNotNull();
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void 캐시_삭제는_트랜잭션이_롤백되면_적용되지_않는다() {
        Cache cache = cacheManager.getCache(CacheConfig.SEATS_SECTION);
        cache.put("key", "value");
        TransactionSynchronizationManager.initSynchronization();

        cache.clear();

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        assertThat(cache.get("key")).isNotNull();
    }
}
