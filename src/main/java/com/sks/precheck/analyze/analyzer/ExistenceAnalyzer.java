package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.ExistencePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 존재형 로그 분석기
 *
 * <p>명세서 - 분석 방식:
 * [존재] : 존재는 부존재일 경우에만 로그가 쌓이므로 에러
 * 즉, 이 로그 자체가 존재한다는 것은 대상 파일/프로세스가 없다는 뜻이므로 항상 에러
 *
 * <p>분석 결과:
 * - LEVEL_ERROR: 항상 에러 (존재형 로그는 문제 상황을 알리는 로그)
 *
 * <p>예시:
 * - LOG_ID="TEST_FILE_MISSING": "test.txt 파일이 존재하지 않음"
 * - 분석 결과: LEVEL_ERROR (파일이 없는 것 자체가 에러)
 *
 * <p>주의: 존재형 정책은 특별한 비교 조건이 없고, 로그가 쌓인 것 자체가 에러입니다.
 *
 * @see LogAnalyzer 분석기 인터페이스
 * @see ExistencePolicy 존재형 정책 DTO
 */\n@Component
public class ExistenceAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(ExistenceAnalyzer.class);

    @Override
    public AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy) {
        if (!(policy instanceof ExistencePolicy)) {
            throw new AnalyzeException("존재형 정책이 아니다: " + policy);
        }

        AnalyzeResult result = baseResult(log);
        result.setAnalyzeLevel(AnalyzeConstants.LEVEL_ERROR);
        result.setAnalyzeMessage("[" + AnalyzeConstants.LEVEL_ERROR + "][" + log.getLogId() + "] " + log.getLogContent());
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
}
