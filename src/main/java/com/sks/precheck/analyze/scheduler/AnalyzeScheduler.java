package com.sks.precheck.analyze.scheduler;

import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.parser.AnalyzeScheduleParser;
import com.sks.precheck.analyze.service.AnalyzeService;
import com.sks.precheck.analyze.vo.AnalyzeScheduleVo;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 로그 분석 스케줄러 — 60초마다 실행, 스케줄 파일(PreCheck_AnalyzeLogs_Schedule.conf)을 읽어 분석 실행 시점을 판별
 *
 * 스케줄 파일은 메모리에 최대 60초 캐싱된다(reloadIntervalMillis).
 * 배치는 지정 요일 지정 시각에 하루 1회만 실행, 주기는 startTime~endTime 사이에 intervalMinutes 간격으로 반복 실행.
 * 각 스케줄 실행의 중복 방지는 lastBatchRunDateByKey / lastPeriodicRunIndexByKey 맵으로 관리한다.
 */
@Component
public class AnalyzeScheduler {

    private static final Logger log = LogManager.getLogger(AnalyzeScheduler.class);

    private static final String DEFAULT_SCHEDULE_FILE_RELATIVE_PATH = "/cfg/PreCheck_AnalyzeLogs_Schedule.conf";

    private final AnalyzeService analyzeService;
    private final AnalyzeScheduleParser analyzeScheduleParser;

    private final String scheduleFilePath;
    private final long reloadIntervalMillis;
    // volatile: @Scheduled 스레드와 캐시 갱신 시점 사이의 가시성 보장
    private volatile long lastReloadAtMillis;
    private volatile List<AnalyzeScheduleVo> cachedSchedules;

    private final Map<String, String> lastBatchRunDateByKey = new HashMap<>();
    private final Map<String, Long> lastPeriodicRunIndexByKey = new HashMap<>();

    public AnalyzeScheduler(
            AnalyzeService analyzeService,
            @Value("${precheck.analyze.schedule-file-path:}") String scheduleFilePath,
            @Value("${precheck.analyze.scheduler.reload-interval-ms:60000}") long reloadIntervalMillis
    ) {
        this.analyzeService = analyzeService;
        this.analyzeScheduleParser = new AnalyzeScheduleParser();
        this.scheduleFilePath = (scheduleFilePath == null || scheduleFilePath.isBlank())
                ? System.getProperty("user.home") + DEFAULT_SCHEDULE_FILE_RELATIVE_PATH
                : scheduleFilePath;
        this.reloadIntervalMillis = reloadIntervalMillis;
    }

    @Scheduled(fixedDelay = 60_000)
    public void run() {
        List<AnalyzeScheduleVo> schedules = getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (AnalyzeScheduleVo schedule : schedules) {
            try {
                if (shouldRun(schedule, now)) {
                    analyzeService.analyze(schedule);
                }
            } catch (Exception e) {
                log.error("분석 스케줄 실행 실패 - 서버: {}, 파일: {}", schedule.getServerId(), schedule.getSourceFilePath(), e);
            }
        }
    }

    private List<AnalyzeScheduleVo> getSchedules() {
        long nowMillis = System.currentTimeMillis();
        if (cachedSchedules != null && nowMillis - lastReloadAtMillis < reloadIntervalMillis) {
            return cachedSchedules;
        }

        try {
            List<AnalyzeScheduleVo> schedules = analyzeScheduleParser.parseScheduleFile(scheduleFilePath);
            cachedSchedules = schedules;
            lastReloadAtMillis = nowMillis;
            return schedules;
        } catch (AnalyzeException e) {
            log.error("분석 스케줄 파일 파싱 실패 - file: {}", scheduleFilePath, e);
            cachedSchedules = List.of();
            lastReloadAtMillis = nowMillis;
            return cachedSchedules;
        }
    }

    private boolean shouldRun(AnalyzeScheduleVo schedule, LocalDateTime now) {
        if (!isTodayMatched(schedule.getDayOfWeek(), now.toLocalDate())) {
            return false;
        }

        int pollWindowSeconds = getPollWindowSeconds();
        int nowSeconds = now.toLocalTime().toSecondOfDay();
        int startSeconds = parseTime(schedule.getStartTime()).toSecondOfDay();

        if ("배치".equals(schedule.getScheduleType())) {
            return shouldRunBatch(schedule, now, nowSeconds, startSeconds, pollWindowSeconds);
        }

        return shouldRunPeriodic(schedule, nowSeconds, startSeconds, pollWindowSeconds);
    }

    private boolean shouldRunBatch(
            AnalyzeScheduleVo schedule,
            LocalDateTime now,
            int nowSeconds,
            int startSeconds,
            int pollWindowSeconds
    ) {
        if (nowSeconds < startSeconds || nowSeconds >= startSeconds + pollWindowSeconds) {
            return false;
        }

        String key = buildScheduleKey(schedule);
        String today = now.toLocalDate().toString();
        String lastRunDate = lastBatchRunDateByKey.get(key);
        if (today.equals(lastRunDate)) {
            return false;
        }

        lastBatchRunDateByKey.put(key, today);
        log.info("배치 분석 실행 결정 - serverId: {}, file: {}, day: {}, start: {}",
                schedule.getServerId(), schedule.getSourceFilePath(), schedule.getDayOfWeek(), schedule.getStartTime());
        return true;
    }

    private boolean shouldRunPeriodic(
            AnalyzeScheduleVo schedule,
            int nowSeconds,
            int startSeconds,
            int pollWindowSeconds
    ) {
        Integer intervalMinutes = schedule.getIntervalMinutes();
        String endTimeText = schedule.getEndTime();
        if (intervalMinutes == null || endTimeText == null || endTimeText.isBlank()) {
            return false;
        }

        int endSeconds = parseTime(endTimeText).toSecondOfDay();
        if (nowSeconds < startSeconds || nowSeconds > endSeconds) {
            return false;
        }

        long intervalSeconds = (long) intervalMinutes * 60L;
        long offsetSeconds = nowSeconds - startSeconds;  // startTime 기준 경과 초

        // runIndex: 몇 번째 주기 구간인지 (0-based)
        // 스케줄러가 60s마다 실행되므로 각 구간의 첫 60s(pollWindowSeconds) 안에만 실행
        long runIndex = offsetSeconds / intervalSeconds;
        long remainder = offsetSeconds % intervalSeconds;
        if (remainder < 0 || remainder >= pollWindowSeconds) {
            return false;
        }

        // 같은 runIndex에서 이미 실행했으면 스킵 (60s 안에 스케줄러가 두 번 이상 실행되는 경우 방지)
        String key = buildScheduleKey(schedule);
        Long lastIndex = lastPeriodicRunIndexByKey.get(key);
        if (lastIndex != null && lastIndex == runIndex) {
            return false;
        }

        lastPeriodicRunIndexByKey.put(key, runIndex);
        log.info("주기 분석 실행 결정 - serverId: {}, file: {}, 간격: {}분, {}번째 실행, day: {}, start: {}, end: {}",
                schedule.getServerId(),
                schedule.getSourceFilePath(),
                intervalMinutes,
                runIndex + 1,
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime());
        return true;
    }

    private int getPollWindowSeconds() {
        return 60;
    }

    private String buildScheduleKey(AnalyzeScheduleVo schedule) {
        return schedule.getServerId()
                + "::" + schedule.getSourceFilePath()
                + "::" + schedule.getScheduleType()
                + "::" + schedule.getDayOfWeek()
                + "::" + schedule.getStartTime()
                + "::" + schedule.getIntervalMinutes()
                + "::" + schedule.getEndTime();
    }

    private boolean isTodayMatched(String daySpec, LocalDate date) {
        if ("*".equals(daySpec)) {
            return true;
        }

        int today = toDayDigit(date.getDayOfWeek());
        if (daySpec != null && daySpec.contains("-")) {
            String[] range = daySpec.split("-", -1);
            if (range.length != 2) {
                return false;
            }
            Integer start = parseDayDigit(range[0].trim());
            Integer end = parseDayDigit(range[1].trim());
            if (start == null || end == null) {
                return false;
            }
            if (start == 0 && end == 6) {
                return true;
            }
            return today >= start && today <= end;
        }

        Integer day = parseDayDigit(daySpec != null ? daySpec.trim() : null);
        return day != null && day == today;
    }

    private Integer parseDayDigit(String text) {
        if (text == null || text.length() != 1) {
            return null;
        }
        char c = text.charAt(0);
        if (c < '0' || c > '6') {
            return null;
        }
        return c - '0';
    }

    private int toDayDigit(DayOfWeek dayOfWeek) {
        // DayOfWeek.getValue()는 ISO 표준 1(월)~7(일). %7로 일요일(7)→0, 월(1)~토(6) 유지
        // conf 파일 요일 표기: 0=일, 1=월, ..., 6=토
        int value = dayOfWeek.getValue();
        return value % 7;
    }

    private LocalTime parseTime(String hhmmss) {
        if (hhmmss == null || hhmmss.length() != 6) {
            throw new AnalyzeException("시간 포맷 오류(HHmmss): " + hhmmss);
        }

        int hh;
        int mm;
        int ss;
        try {
            hh = Integer.parseInt(hhmmss.substring(0, 2));
            mm = Integer.parseInt(hhmmss.substring(2, 4));
            ss = Integer.parseInt(hhmmss.substring(4, 6));
        } catch (NumberFormatException e) {
            throw new AnalyzeException("시간 포맷 오류(HHmmss): " + hhmmss);
        }

        return LocalTime.of(hh, mm, ss);
    }
}
