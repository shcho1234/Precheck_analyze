package com.sks.precheck.analyze;

import static org.junit.jupiter.api.Assertions.*;

import com.sks.precheck.analyze.analyzer.PhraseAnalyzer;
import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.PhrasePolicy;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhraseAnalyzerTest {

    private final PhraseAnalyzer analyzer = new PhraseAnalyzer();

    @Test
    void keywordIncluded_isError() {
        PhrasePolicy policy = new PhrasePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("PUSH_STATUS");
        policy.setErrorKeywords(List.of("실패", "오류"));

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_TEXT);
        log.setLogId("PUSH_STATUS");
        log.setLogContent("PUSH 전송 실패(PUSH 서버 실패 O)");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_ERROR, result.getAnalyzeLevel());
        assertTrue(result.getAnalyzeMessage().contains("(키워드: 실패)"));
    }

    @Test
    void keywordNotIncluded_isNormal() {
        PhrasePolicy policy = new PhrasePolicy();
        policy.setServerId("dlprem01-테스트개발");
        policy.setLogId("PUSH_STATUS");
        policy.setErrorKeywords(List.of("실패", "오류"));

        CollectLog log = baseCollectLog();
        log.setLogType(AnalyzeConstants.LOG_TYPE_TEXT);
        log.setLogId("PUSH_STATUS");
        log.setLogContent("PUSH 전송 완료");

        AnalyzeResult result = analyzer.analyze(log, policy);
        assertEquals(AnalyzeConstants.LEVEL_NORMAL, result.getAnalyzeLevel());
        assertFalse(result.getAnalyzeMessage().contains("키워드:"));
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

