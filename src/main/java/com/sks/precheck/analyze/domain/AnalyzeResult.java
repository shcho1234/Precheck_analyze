package com.sks.precheck.analyze.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 분석 결과 DTO (TB_ANALYZE_RESULT와 매핑)
 *
 * <p>이 클래스의 필드들은 DB 테이블 TB_ANALYZE_RESULT의 컬럼과 일대일로 매핑됩니다.
 * 분석 서버는 각 수집 로그 건(CollectLog)을 분석한 결과를 이 DTO로 구성한 뒤
 * {@code AnalyzeResultMapper.insert()}를 통해 DB에 저장합니다.
 *
 * 주요 필드 설명:
 * - analyzeResultId: SEQ_ANALYZE_RESULT로 생성되는 PK
 * - collectLogId: 원본 TB_COLLECT_LOG의 PK
 * - analyzeLevel: '정상'|'경고'|'에러'|'정보'|'미분석'
 * - analyzeMessage: 분석 결과 설명 메시지 (대시보드/통보에 사용)
 * - thresholdValue/thresholdOperator/warningRatio: 수치형 분석 상세 정보
 */
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
        this.logValue = logValue;
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
