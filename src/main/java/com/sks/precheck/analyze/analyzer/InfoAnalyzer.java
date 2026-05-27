package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.InfoPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 정보형 로그 분석기
 *
 * <p>명세서 - 분석 방식:
 * [정보] : 분석 레벨이 아닌 저장용 타입이며 분석 결과 레벨에 포함되지 않는다
 * 즉, 분석을 수행하지 않고 정보 내용 그대로 저장만 함
 *
 * <p>분석 결과:
 * - LEVEL_INFO: 항상 정보 (분석 없음, 저장만)
 *
 * <p>예시:
 * - LOG_ID="DAILY_REPORT": "일일 처리 리포트 저장 완료"
 * - 분석 결과: LEVEL_INFO (분석 불필요, Dashboard 조회 용도)
 *
 * <p>용도: Dashboard에서 시스템 상태/진행 상황 조회, 통보 서버에서 제외
 *
 * @see LogAnalyzer 분석기 인터페이스
 * @see InfoPolicy 정보형 정책 DTO
 */\n@Component
public class InfoAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(InfoAnalyzer.class);

    @Override
    public AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy) {
        if (!(policy instanceof InfoPolicy)) {
            throw new AnalyzeException("정보형 정책이 아니다: " + policy);
        }

        AnalyzeResult result = baseResult(log);
        result.setAnalyzeLevel(AnalyzeConstants.LEVEL_INFO);
        result.setAnalyzeMessage("[" + AnalyzeConstants.LEVEL_INFO + "][" + log.getLogId() + "] " + log.getLogContent());
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
