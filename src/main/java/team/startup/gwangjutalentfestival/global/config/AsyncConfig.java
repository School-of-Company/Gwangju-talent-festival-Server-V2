package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리를 위한 스레드 풀 설정.
 * <p>코어 5개, 최대 10개의 스레드와 큐 용량 100을 갖는 {@code asyncExecutor} 빈을 등록한다.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업 전용 스레드 풀 Executor를 생성한다.
     *
     * @return 설정된 {@link Executor} 빈
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-executor-");
        executor.initialize();
        return executor;
    }
}
