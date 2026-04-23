package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * SSE 하트비트 전송에 사용되는 {@link TaskScheduler} 빈 설정.
 * <p>풀 크기는 가용 CPU 코어 수에 비례하여 동적으로 결정된다.</p>
 */
@Configuration
public class SchedulerConfig {

    /**
     * SSE 하트비트 전용 스레드 풀 스케줄러를 생성한다.
     * 풀 크기 = max(4, CPU 코어 수 × 2)
     *
     * @return {@link TaskScheduler} 빈
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("sse-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}
