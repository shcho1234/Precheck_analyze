package com.sks.precheck.analyze.vo;

/**
 * 분석 스케줄 VO
 *
 * <p>PreCheck_AnalyzeLogs_Schedule.conf의 각 스케줄 항목을 메모리 내에서 표현하는 VO입니다.
 * 필드 설명:
 * - serverId: 분석 대상 서버 식별자
 * - sourceFilePath: 수집된 로그 파일 경로
 * - scheduleType: "배치" 또는 "주기"
 * - dayOfWeek: 배치 실행 요일 (예: MON,TUE)
 * - startTime: 실행 시작 시각 (HHmm 또는 HH:mm)
 * - intervalMinutes: 주기 실행 시 반복 간격(분)
 * - endTime: 주기 실행 종료 시각
 */
public class AnalyzeScheduleVo {

    private String serverId;
    private String sourceFilePath;
    private String scheduleType;
    private String dayOfWeek;
    private String startTime;
    private Integer intervalMinutes;
    private String endTime;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(Integer intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
