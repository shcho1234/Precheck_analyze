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
 * 로그 분석 스케줄러
 *
 * <p>역할:
 * - 매 1분마다 (@Scheduled fixedDelay=60초) 실행
 * - PreCheck_AnalyzeLogs_Schedule.conf 파일에서 분석 스케줄 로드
 * - 배치/주기 스케줄별로 실행 시기 판별
 * - 실행 대상 스케줄을 AnalyzeService에 위임
 *
 * <p>스케줄 파일 형식 (PreCheck_AnalyzeLogs_Schedule.conf):
 * [serverId][sourceFilePath][스케줄타입][요일][시간][분]
 * - 배치: [SRV001][/logs/system.log][배치][MON,TUE,...][09][00]
 * - 주기: [SRV001][/logs/system.log][주기][월-일 범위][반복간격(분)]
 *
 * <p>스케줄 실행 판별:
 * 1. 스케줄 파일에서 모든 스케줄 로드 (1분마다, 캐시 60초)
 * 2. 각 스케줄마다:
 *    - 오늘 요일이 스케줄 요일에 포함되는가? (배치용)
 *    - 현재 시간이 스케줄 시간 범위 내인가? (1분 단위 poll window: +/- 30초)
 *    - 배치: 오늘 첫 1회만 실행 (lastBatchRunDate로 중복 실행 방지)
 *    - 주기: 주기 간격으로 반복 실행
 *
 * <p>실행 흐름:
 * 1. 스케줄 파일 로드 (메모리 캐시, 60초 유효)
 * 2. 각 스케줄 shouldRun 판별
 * 3. shouldRun=true인 스케줄 AnalyzeService.analyze() 호출
 * 4. 예외 발생 시 로그만 기록하고 다음 스케줄 처리
 *
 * <p>캐시 전략:
 * - 스케줄 파일은 최대 60초마다 재로드 (자주 변경되지 않는 설정 파일)
 * - 마지막 배치 실행 날짜는 메모리에 보관 (중복 실행 방지)\n */\n@Component
public class AnalyzeScheduler {

    private static final Logger log = LogManager.getLogger(AnalyzeScheduler.class);

    private static final String DEFAULT_SCHEDULE_FILE_RELATIVE_PATH = "/cfg/PreCheck_AnalyzeLogs_Schedule.conf";

    private final AnalyzeService analyzeService;
    private final AnalyzeScheduleParser analyzeScheduleParser;

    private final String scheduleFilePath;
    private final long reloadIntervalMillis;
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
        long offsetSeconds = nowSeconds - startSeconds;

        long runIndex = offsetSeconds / intervalSeconds;
        long remainder = offsetSeconds % intervalSeconds;
        if (remainder < 0 || remainder >= pollWindowSeconds) {
            return false;
        }

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
