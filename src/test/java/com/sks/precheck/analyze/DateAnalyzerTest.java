package com.sks.precheck.analyze;

import static org.junit.jupiter.api.Assertions.*;

import com.sks.precheck.analyze.analyzer.DateAnalyzer;
import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.DatePolicy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class DateAnalyzerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final DateAnalyzer analyzer = new DateAnalyzer();

    @Test
    void today_isNormal() {
        String today = LocalDate.now().format(FORMATTER);

        DatePolicy policy = new DatePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("DATE_BDAY");

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_DATE);
        log.setLogId("DATE_BDAY");
        log.setLogContent("bday[" + today + "]");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_NORMAL, result.getAnalyzeLevel());
    }

    @Test
    void otherDate_isError() {
        String other = "1999/01/01";

        DatePolicy policy = new DatePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("DATE_BDAY");

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_DATE);
        log.setLogId("DATE_BDAY");
        log.setLogContent("bday[" + other + "]");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_ERROR, result.getAnalyzeLevel());
        assertTrue(result.getAnalyzeMessage().contains("날짜 불일치"));
    }

    private CollectLog baseCollectLog() {
        CollectLog log = new CollectLog();
        log.setCollectLogId(1L);
        log.setServerId("dlprem01-테스트개발");
        log.setServerIp("127.0.0.1");
        log.setSourceFilePath("/tmp/check.out");
        log.setLogTimestamp(LocalDateTime.now());
        log.setCollectDate("20260527");
        log.setCollectDatetime(LocalDateTime.now());
        log.setScheduleType("배치");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}

