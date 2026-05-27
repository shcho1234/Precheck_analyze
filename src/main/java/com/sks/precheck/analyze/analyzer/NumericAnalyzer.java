package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.NumericPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 수치형 로그 분석기
 *
 * <p>명세서 - 분석 방식:
 * [수치] : LOG_ID에 매칭된 임계수치와 >, >=, <, <=, = 연산 비교
 * - 참이면 정상
 * - 거짓이면 에러
 * - 임계수치 근접에 도달했을 경우 경고 (임계수치의 warningRatio% 이내)
 *
 * <p>분석 결과:
 * - LEVEL_NORMAL: 로그값이 조건식을 만족하고 경고 범위 밖
 * - LEVEL_WARNING: 로그값이 조건식 만족하지만 임계수치 근접 (경고 비율 내)
 * - LEVEL_ERROR: 로그값이 조건식을 만족하지 않음 또는 로그값 NULL
 *
 * <p>예시:
 * - 정책: operator="<", threshold=100, warningRatio=20
 * - 로그값 50: LEVEL_NORMAL (50 < 100이고 경고 범위 밖)
 * - 로그값 95: LEVEL_WARNING (95 < 100이지만 경고 범위 80~100 내)
 * - 로그값 110: LEVEL_ERROR (110 < 100 거짓)
 *
 * <p>주의: 수치 계산은 반드시 BigDecimal 사용 (float/double 금지)
 *
 * @see LogAnalyzer 분석기 인터페이스
 * @see NumericPolicy 수치형 정책 DTO
 */
@Component
public class NumericAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(NumericAnalyzer.class);

    // BigDecimal 상수: 백분율 계산용 (임계수치 근접 비율 계산)
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    // 계산 시 소수점 자리수 (나누기 결과 스케일)
    private static final int CALC_SCALE = 6;

    @Override
    public AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy) {
        if (!(policy instanceof NumericPolicy)) {
            throw new AnalyzeException("수치형 정책이 아니다: " + policy);
        }

        NumericPolicy numericPolicy = (NumericPolicy) policy;

        BigDecimal logValue = log.getLogValue();
        if (logValue == null) {
            AnalyzeResult result = baseResult(log);
            result.setAnalyzeLevel(AnalyzeConstants.LEVEL_ERROR);
            result.setAnalyzeMessage("[" + AnalyzeConstants.LEVEL_ERROR + "][" + log.getLogId() + "] "
                    + log.getLogContent() + " (수치값 없음)");
            result.setThresholdValue(numericPolicy.getThreshold());
            result.setThresholdOperator(numericPolicy.getOperator());
            result.setWarningRatio(numericPolicy.getWarningRatio());
            return result;
        }

        BigDecimal threshold = numericPolicy.getThreshold();
        BigDecimal warningRatio = numericPolicy.getWarningRatio();
        String operator = numericPolicy.getOperator();

        if (threshold == null || warningRatio == null || operator == null || operator.isBlank()) {
            throw new AnalyzeException("수치형 정책 필수값 누락 - serverId: " + numericPolicy.getServerId()
                    + ", logId: " + numericPolicy.getLogId());
        }

        String level = decideLevel(logValue, operator.trim(), threshold, warningRatio);

        AnalyzeResult result = baseResult(log);
        result.setAnalyzeLevel(level);
        result.setThresholdValue(threshold);
        result.setThresholdOperator(operator.trim());
        result.setWarningRatio(warningRatio);
        result.setAnalyzeMessage(buildMessage(level, log.getLogId(), log.getLogContent(), logValue, operator.trim(), threshold, warningRatio));
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

    private String decideLevel(BigDecimal logValue, String operator, BigDecimal threshold, BigDecimal warningRatio) {
        if ("=".equals(operator)) {
            return logValue.compareTo(threshold) == 0 ? AnalyzeConstants.LEVEL_NORMAL : AnalyzeConstants.LEVEL_ERROR;
        }

        BigDecimal warningDelta = threshold
                .multiply(warningRatio)
                .divide(ONE_HUNDRED, CALC_SCALE, RoundingMode.HALF_UP);

        if ("<".equals(operator) || "<=".equals(operator)) {
            boolean isError = "<".equals(operator)
                    ? logValue.compareTo(threshold) >= 0
                    : logValue.compareTo(threshold) > 0;

            if (isError) {
                return AnalyzeConstants.LEVEL_ERROR;
            }

            BigDecimal warningStart = threshold.subtract(warningDelta);
            boolean isWarning = logValue.compareTo(warningStart) >= 0;
            return isWarning ? AnalyzeConstants.LEVEL_WARNING : AnalyzeConstants.LEVEL_NORMAL;
        }

        if (">".equals(operator) || ">=".equals(operator)) {
            boolean isError = ">".equals(operator)
                    ? logValue.compareTo(threshold) <= 0
                    : logValue.compareTo(threshold) < 0;

            if (isError) {
                return AnalyzeConstants.LEVEL_ERROR;
            }

            BigDecimal warningEnd = threshold.add(warningDelta);
            boolean isWarning = logValue.compareTo(warningEnd) <= 0;
            return isWarning ? AnalyzeConstants.LEVEL_WARNING : AnalyzeConstants.LEVEL_NORMAL;
        }

        throw new AnalyzeException("지원하지 않는 연산자: " + operator);
    }

    private String buildMessage(
            String level,
            String logId,
            String content,
            BigDecimal logValue,
            String operator,
            BigDecimal threshold,
            BigDecimal warningRatio
    ) {
        if (AnalyzeConstants.LEVEL_WARNING.equals(level)) {
            return "[" + level + "][" + logId + "] " + content + " " + logValue + " " + operator + " " + threshold
                    + "(임계치 대비 " + warningRatio.stripTrailingZeros().toPlainString() + "% 근접)";
        }
        if (AnalyzeConstants.LEVEL_ERROR.equals(level)) {
            return "[" + level + "][" + logId + "] " + content + " " + logValue + " " + operator + " " + threshold + "(임계치)";
        }
        return "[" + level + "][" + logId + "] " + content + " " + logValue + " " + operator + " " + threshold + "(임계치)";
    }
}
