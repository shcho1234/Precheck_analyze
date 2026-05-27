package com.sks.precheck.analyze.config;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
/**
 * 재시도 설정
 *
 * <p>기본적으로 AnalyzeRetryService에서 요구하는 재시도 정책을 적용하기 위해
 * {@link org.springframework.retry.support.RetryTemplate}을 생성합니다.
 * - 재시도 횟수: AnalyzeConstants.MAX_RETRY_COUNT + 1 (최초 1회 포함)
 * - 재시도 간격: AnalyzeConstants.RETRY_DELAY_MILLISECONDS (예: 300_000L = 5분)
 *
 * <p>이 설정은 재시도 로직을 커스텀으로 구성해야 할 때 확장 포인트로 사용됩니다.
 */
public class RetryConfig {

    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(AnalyzeConstants.RETRY_DELAY_MILLISECONDS);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(AnalyzeConstants.MAX_RETRY_COUNT + 1);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
