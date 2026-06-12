package com.sks.precheck.analyze.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class AnalyzeResult {

    private Long analyzeResultId;
    private Long collectLogId;

    private String serverId;
    private String serverIp;

    private String logType;
    private String logId;
    private LocalDateTime logTimestamp;
    private String logContent;
    private BigDecimal logValue;

    private String analyzeLevel;
    private String analyzeMessage;

    private BigDecimal thresholdValue;
    private String thresholdOperator;
    private BigDecimal warningRatio;

    private String analyzeDate;
    private LocalDateTime analyzeDatetime;
    private String collectDate;

    private String notifyYn;
    private LocalDateTime notifyAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getAnalyzeResultId() {
        return analyzeResultId;
    }

    public void setAnalyzeResultId(Long analyzeResultId) {
        this.analyzeResultId = analyzeResultId;
    }

    public Long getCollectLogId() {
        return collectLogId;
    }

    public void setCollectLogId(Long collectLogId) {
        this.collectLogId = collectLogId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public LocalDateTime getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(LocalDateTime logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public BigDecimal getLogValue() {
        return logValue;
    }

    public void setLogValue(BigDecimal logValue) {
        this.logValue = logValue == null ? null : logValue.setScale(2, RoundingMode.DOWN);
    }

    public String getAnalyzeLevel() {
        return analyzeLevel;
    }

    public void setAnalyzeLevel(String analyzeLevel) {
        this.analyzeLevel = analyzeLevel;
    }

    public String getAnalyzeMessage() {
        return analyzeMessage;
    }

    public void setAnalyzeMessage(String analyzeMessage) {
        this.analyzeMessage = analyzeMessage;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getThresholdOperator() {
        return thresholdOperator;
    }

    public void setThresholdOperator(String thresholdOperator) {
        this.thresholdOperator = thresholdOperator;
    }

    public BigDecimal getWarningRatio() {
        return warningRatio;
    }

    public void setWarningRatio(BigDecimal warningRatio) {
        this.warningRatio = warningRatio;
    }

    public String getAnalyzeDate() {
        return analyzeDate;
    }

    public void setAnalyzeDate(String analyzeDate) {
        this.analyzeDate = analyzeDate;
    }

    public LocalDateTime getAnalyzeDatetime() {
        return analyzeDatetime;
    }

    public void setAnalyzeDatetime(LocalDateTime analyzeDatetime) {
        this.analyzeDatetime = analyzeDatetime;
    }

    public String getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(String collectDate) {
        this.collectDate = collectDate;
    }

    public String getNotifyYn() {
        return notifyYn;
    }

    public void setNotifyYn(String notifyYn) {
        this.notifyYn = notifyYn;
    }

    public LocalDateTime getNotifyAt() {
        return notifyAt;
    }

    public void setNotifyAt(LocalDateTime notifyAt) {
        this.notifyAt = notifyAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
