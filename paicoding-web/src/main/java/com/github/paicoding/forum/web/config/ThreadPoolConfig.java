package com.github.paicoding.forum.web.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 开启异步支持
public class ThreadPoolConfig {

    /**
     * ✅ D43 新增：MDC 跨线程传递装饰器
     * 作用：让子线程继承主线程的 TraceId
     */
    public static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 1. 在主线程 (提交任务前) 抓取 MDC
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                // 2. 在子线程 (执行任务时) 恢复 MDC
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // 3. 必须清理，防止复用线程导致的脏数据
                    MDC.clear();
                }
            };
        }
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：根据你的机器核数设定，通常 io 密集型设为 2n
        executor.setCorePoolSize(4);
        // 最大线程数
        executor.setMaxPoolSize(8);
        // 队列大小
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("Ai-Async-");
        // 拒绝策略：调用者运行 (防止丢任务)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // ✅ D43 关键：挂载 MDC 装饰器
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        return executor;
    }
}