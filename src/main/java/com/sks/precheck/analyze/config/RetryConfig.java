package com.sks.precheck.analyze.config;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
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
