package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * SSE 하트비트 전송에 사용되는 {@link TaskScheduler} 빈 설정.
 * <p>스레드 풀 크기 4로 구성된 스케줄러를 등록한다.</p>
 */
@Configuration
public class SchedulerConfig {

    /**
     * SSE 하트비트 전용 스레드 풀 스케줄러를 생성한다.
     *
     * @return {@link TaskScheduler} 빈
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("sse-heartbeat-");
        return scheduler;
    }
}
