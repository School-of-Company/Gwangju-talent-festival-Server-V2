package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

/**
 * SSE 하트비트 전송에 사용되는 {@link TaskScheduler} 빈 설정.
 * <p>가상 스레드 기반 {@link SimpleAsyncTaskScheduler}를 사용해 스레드 상한 없이 스케줄링한다.</p>
 */
@Configuration
public class SchedulerConfig {

    /**
     * SSE 하트비트 전용 가상 스레드 스케줄러를 생성한다.
     *
     * @return {@link TaskScheduler} 빈
     */
    @Bean
    public TaskScheduler taskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("sse-heartbeat-");
        return scheduler;
    }
}
