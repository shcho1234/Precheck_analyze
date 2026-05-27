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

@Component
public class NumericAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(NumericAnalyzer.class);

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
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
