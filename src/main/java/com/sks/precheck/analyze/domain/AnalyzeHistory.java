package com.sks.precheck.analyze.domain;

import java.time.LocalDateTime;

/**
 * 분석 실행 이력 DTO (TB_ANALYZE_HISTORY와 매핑)
 *
 * <p>분석 실행 단위(스케줄 단위) 별로 수행 결과 요약을 DB에 저장하기 위한 DTO입니다.
 * - analyzeHistoryId: SEQ_ANALYZE_HISTORY로 생성되는 PK
 * - totalCount/successCount/failCount 등: 분석 통계
 * - lastAnalyzeLogId: 이번 실행에서 마지막으로 처리한 COLLECT_LOG_ID
 * - analyzeStatus: SUCCESS/FAIL/PARTIAL
 * - failReason: 실패 사유 기록
 */
public class AnalyzeHistory {

    private Long analyzeHistoryId;

    private String serverId;
    private String analyzeTargetDate;
    private String sourceFilePath;

    private String analyzeStatus;
    private Long totalCount;
    private Long successCount;
    private Long failCount;
    private Long errorCount;
    private Long warningCount;

    private Long lastAnalyzeLogId;
    private String failReason;

    private LocalDateTime analyzeStartAt;
    private LocalDateTime analyzeEndAt;
    private String analyzeDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getAnalyzeHistoryId() {
        return analyzeHistoryId;
    }

    public void setAnalyzeHistoryId(Long analyzeHistoryId) {
        this.analyzeHistoryId = analyzeHistoryId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getAnalyzeTargetDate() {
        return analyzeTargetDate;
    }

    public void setAnalyzeTargetDate(String analyzeTargetDate) {
        this.analyzeTargetDate = analyzeTargetDate;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getAnalyzeStatus() {
        return analyzeStatus;
    }

    public void setAnalyzeStatus(String analyzeStatus) {
        this.analyzeStatus = analyzeStatus;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public Long getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Long warningCount) {
        this.warningCount = warningCount;
    }

    public Long getLastAnalyzeLogId() {
        return lastAnalyzeLogId;
    }

    public void setLastAnalyzeLogId(Long lastAnalyzeLogId) {
        this.lastAnalyzeLogId = lastAnalyzeLogId;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public LocalDateTime getAnalyzeStartAt() {
        return analyzeStartAt;
    }

    public void setAnalyzeStartAt(LocalDateTime analyzeStartAt) {
        this.analyzeStartAt = analyzeStartAt;
    }

    public LocalDateTime getAnalyzeEndAt() {
        return analyzeEndAt;
    }

    public void setAnalyzeEndAt(LocalDateTime analyzeEndAt) {
        this.analyzeEndAt = analyzeEndAt;
    }

    public String getAnalyzeDate() {
        return analyzeDate;
    }

    public void setAnalyzeDate(String analyzeDate) {
        this.analyzeDate = analyzeDate;
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
