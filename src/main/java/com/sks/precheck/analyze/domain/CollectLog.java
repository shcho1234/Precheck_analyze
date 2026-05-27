package com.sks.precheck.analyze.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 수집된 로그 DTO (TB_COLLECT_LOG과 매핑)
 *
 * <p>수집 서버가 정규화하여 저장한 로그 레코드를 분석 서버에서 조회할 때 사용되는 DTO입니다.
 * 주요 필드:
 * - collectLogId: TB_COLLECT_LOG PK
 * - serverId/serverIp: 로그가 생성된 서버 정보
 * - logType/logId: 입력 타입(문구/수치/날짜/존재/정보) 및 로그 식별자
 * - logTimestamp/logContent/logValue: 로그 발생 시각, 내용, 수치값(수치형일 경우)
 * - rawLog: 원문 로그 (필요 시)
 */
public class CollectLog {

    private Long collectLogId;
    private String serverId;
    private String serverIp;
    private String sourceFilePath;
    private String logType;
    private String logId;
    private LocalDateTime logTimestamp;
    private String logContent;
    private BigDecimal logValue;
    private String rawLog;
    private String collectDate;
    private LocalDateTime collectDatetime;
    private String scheduleType;
    private Long lineNumber;
    private LocalDateTime createdAt;

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

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
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
        this.logValue = logValue;
    }

    public String getRawLog() {
        return rawLog;
    }

    public void setRawLog(String rawLog) {
        this.rawLog = rawLog;
    }

    public String getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(String collectDate) {
        this.collectDate = collectDate;
    }

    public LocalDateTime getCollectDatetime() {
        return collectDatetime;
    }

    public void setCollectDatetime(LocalDateTime collectDatetime) {
        this.collectDatetime = collectDatetime;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
