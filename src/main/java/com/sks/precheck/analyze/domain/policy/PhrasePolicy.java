package com.sks.precheck.analyze.domain.policy;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import java.util.ArrayList;
import java.util.List;

public class PhrasePolicy implements AnalyzePolicy {

    private String serverId;
    private String logId;
    private final String logType = AnalyzeConstants.LOG_TYPE_TEXT;
    private List<String> errorKeywords = new ArrayList<>();

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

    public List<String> getErrorKeywords() {
        return errorKeywords;
    }

    public void setErrorKeywords(List<String> errorKeywords) {
        this.errorKeywords = errorKeywords;
    }
}
