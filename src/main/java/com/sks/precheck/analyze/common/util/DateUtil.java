package com.sks.precheck.analyze.common.util;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern(AnalyzeConstants.LOG_TIMESTAMP_FORMAT);

    private static final DateTimeFormatter ANALYZE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(AnalyzeConstants.ANALYZE_DATE_FORMAT);

    private DateUtil() {
    }

    public static LocalDateTime parseLogTimestamp(String timestampText) {
        try {
            return LocalDateTime.parse(timestampText, LOG_TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new AnalyzeException("로그 timestamp 파싱 실패: " + timestampText, e);
        }
    }

    public static String formatAnalyzeDate(LocalDate date) {
        return date.format(ANALYZE_DATE_FORMATTER);
    }

    public static String todayAnalyzeDate() {
        return formatAnalyzeDate(LocalDate.now());
    }
}
