package com.library.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures a dedicated thread pool for asynchronous notification tasks.
 *
 * Using a named executor ("notificationExecutor") instead of the default one
 * ensures that notification work is isolated from the main request-handling
 * threads, preventing any slowdown or resource contention.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Minimum threads always kept alive
        executor.setCorePoolSize(2);

        // Maximum threads allowed under high load
        executor.setMaxPoolSize(5);

        // Queue capacity before rejecting tasks
        executor.setQueueCapacity(100);

        // Prefix helps identify these threads in logs
        executor.setThreadNamePrefix("notification-");

        // Wait for active tasks to finish before shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
