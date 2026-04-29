package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * 비동기 처리를 위한 Executor 설정.
 * <p>가상 스레드 기반 {@link VirtualThreadTaskExecutor}를 {@code asyncExecutor} 빈으로 등록한다.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업 전용 가상 스레드 Executor를 생성한다.
     *
     * @return {@link Executor} 빈
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return new VirtualThreadTaskExecutor("async-executor-");
    }
}
