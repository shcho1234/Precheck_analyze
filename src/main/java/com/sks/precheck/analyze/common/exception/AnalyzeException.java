package com.sks.precheck.analyze.common.exception;

/**
 * 분석 서버 전용 런타임 예외
 *
 * <p>비즈니스 로직에서 발생하는 예외를 감싸서 상위 계층으로 전파할 때 사용합니다.
 * AnalyzeRetryService는 이 예외를 기준으로 재시도 동작을 수행합니다.
 */
public class AnalyzeException extends RuntimeException {

    public AnalyzeException(String message) {
        super(message);
    }

    public AnalyzeException(String message, Throwable cause) {
        super(message, cause);
    }
}
