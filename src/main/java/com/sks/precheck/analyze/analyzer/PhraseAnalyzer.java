package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.PhrasePolicy;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 문구형 로그 분석기
 *
 * <p>명세서 - 분석 방식:
 * [문구] : LOG_ID에 매칭된 에러 키워드를 포함하면 에러, 포함하지 않으면 정상
 *
 * <p>분석 결과:
 * - LEVEL_NORMAL: 에러 키워드가 로그에 포함되지 않음
 * - LEVEL_ERROR: 에러 키워드 중 하나라도 로그에 포함됨 (첫 번째 일치 키워드 메시지에 포함)
 *
 * <p>예시:
 * - 정책: serverId="SRV001", logId="ERROR_CHECK", keywords=["ORA-", "Exception"]
 * - 로그: "2026/05/27 12:00:00.000 ORA-1234 에러발생"
 * - 결과: LEVEL_ERROR ("ORA-" 키워드 발견)
 *
 * @see LogAnalyzer 분석기 인터페이스
 * @see PhrasePolicy 문구형 정책 DTO
 */\n@Component
public class PhraseAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(PhraseAnalyzer.class);

    @Override
    public AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy) {
        if (!(policy instanceof PhrasePolicy)) {
            throw new AnalyzeException("문구형 정책이 아니다: " + policy);
        }

        PhrasePolicy phrasePolicy = (PhrasePolicy) policy;
        String logContent = log.getLogContent();

        String matchedKeyword = findMatchedKeyword(logContent, phrasePolicy.getErrorKeywords());
        String level = matchedKeyword == null ? AnalyzeConstants.LEVEL_NORMAL : AnalyzeConstants.LEVEL_ERROR;

        AnalyzeResult result = baseResult(log);
        result.setAnalyzeLevel(level);
        result.setAnalyzeMessage(buildMessage(level, log.getLogId(), logContent, matchedKeyword));
        return result;
    }

    private AnalyzeResult baseResult(CollectLog log) {
        AnalyzeResult result = new AnalyzeResult();
        result.setCollectLogId(log.getCollectLogId());
        result.setServerId(log.getServerId());
        result.setServerIp(log.getServerIp());
        result.setLogType(log.getLogType());
        result.setLogId(log.getLogId());
        result.setLogTimestamp(log.getLogTimestamp());
        result.setLogContent(log.getLogContent());
        result.setLogValue(log.getLogValue());
        return result;
    }

    private String findMatchedKeyword(String content, List<String> keywords) {
        if (content == null || keywords == null || keywords.isEmpty()) {
            return null;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    private String buildMessage(String level, String logId, String content, String matchedKeyword) {
        if (AnalyzeConstants.LEVEL_ERROR.equals(level)) {
            return "[" + level + "][" + logId + "] " + content + " (키워드: " + matchedKeyword + ")";
        }
        return "[" + level + "][" + logId + "] " + content;
    }
}
