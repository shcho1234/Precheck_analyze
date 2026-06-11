package com.sks.precheck.analyze.domain.policy;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;

public class ComparePolicy implements AnalyzePolicy {

    private String serverId;
    private String logId;
    private final String logType = AnalyzeConstants.LOG_TYPE_COMPARE;

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
}

