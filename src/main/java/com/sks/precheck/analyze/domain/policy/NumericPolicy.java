package com.sks.precheck.analyze.domain.policy;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import java.math.BigDecimal;

public class NumericPolicy implements AnalyzePolicy {

    private String serverId;
    private String logId;
    private final String logType = AnalyzeConstants.LOG_TYPE_NUMERIC;

    private String operator;
    private BigDecimal threshold;
    private BigDecimal warningRatio;

    @Override
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    @Override
    public String getLogType() {
        return logType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public BigDecimal getWarningRatio() {
        return warningRatio;
    }

    public void setWarningRatio(BigDecimal warningRatio) {
        this.warningRatio = warningRatio;
    }
}
