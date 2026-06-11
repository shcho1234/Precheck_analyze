package com.sks.precheck.analyze;

import static org.junit.jupiter.api.Assertions.*;

import com.sks.precheck.analyze.analyzer.TimeAnalyzer;
import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.TimePolicy;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TimeAnalyzerTest {

    private final TimeAnalyzer analyzer = new TimeAnalyzer();

    @Test
    void beforeThreshold_isNormal() {
        TimePolicy policy = new TimePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("DATE_BTIME");
        policy.setOperator("<");
        policy.setThresholdTime("08:00");

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_TIME);
        log.setLogId("DATE_BTIME");
        log.setLogContent("처리시간 $07:35$");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_NORMAL, result.getAnalyzeLevel());
        assertTrue(result.getAnalyzeMessage().contains("< 08:00"));
    }

    @Test
    void afterThreshold_isError() {
        TimePolicy policy = new TimePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("DATE_BTIME");
        policy.setOperator("<");
        policy.setThresholdTime("08:00");

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_TIME);
        log.setLogId("DATE_BTIME");
        log.setLogContent("처리시간 $09:10$");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_ERROR, result.getAnalyzeLevel());
        assertTrue(result.getAnalyzeMessage().contains(">= 08:00"));
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

