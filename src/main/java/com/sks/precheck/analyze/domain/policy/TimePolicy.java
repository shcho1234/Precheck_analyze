package com.sks.precheck.analyze.domain.policy;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;

public class TimePolicy implements AnalyzePolicy {

    private String serverId;
    private String logId;
    private final String logType = AnalyzeConstants.LOG_TYPE_TIME;

    private String operator;
    private String thresholdTime;

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

    public String getThresholdTime() {
        return thresholdTime;
    }

    public void setThresholdTime(String thresholdTime) {
        this.thresholdTime = thresholdTime;
    }
}

