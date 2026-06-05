package com.sks.precheck.analyze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 분석 비동기 실행 스레드풀 설정
 *
 * 각 스케줄은 별도 스레드에서 실행되므로 한 스케줄의 재시도 대기(10s)가
 * 다른 스케줄을 블로킹하지 않는다.
 *
 * corePoolSize=5  : 평시 동시 분석 수
 * maxPoolSize=20  : 최대 동시 분석 수 (명세 최대 서버 수 100 기준 여유있게 설정)
 * queueCapacity=50: 스레드풀 포화 시 대기 큐 크기
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "analyzeTaskExecutor")
    public ThreadPoolTaskExecutor analyzeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("analyze-async-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
}
