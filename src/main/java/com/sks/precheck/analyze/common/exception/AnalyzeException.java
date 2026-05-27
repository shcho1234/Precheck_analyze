package com.sks.precheck.analyze.common.exception;

public class AnalyzeException extends RuntimeException {

    public AnalyzeException(String message) {
        super(message);
    }

    public AnalyzeException(String message, Throwable cause) {
        super(message, cause);
    }
}
