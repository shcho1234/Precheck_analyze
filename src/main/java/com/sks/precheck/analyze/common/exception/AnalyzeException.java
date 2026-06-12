package com.sks.precheck.analyze.common.exception;

// @Retryable(retryFor = AnalyzeException.class) 의 트리거 예외
public class AnalyzeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AnalyzeException(String message) {
        super(message);
    }

    public AnalyzeException(String message, Throwable cause) {
        super(message, cause);
    }
}
