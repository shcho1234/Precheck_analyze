package com.sks.precheck.analyze.parser;

import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.vo.AnalyzeScheduleVo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 분석 스케줄 파일(PreCheck_AnalyzeLogs_Schedule.conf) 파서
 *
 * 포맷: [serverId][sourceFilePath][배치|주기|...]  또는 [serverId][배치|주기|...]
 * serverId+sourceFilePath가 중복되면 파일 내 마지막 정의가 최종으로 적용된다(LinkedHashMap remove→put).
 */
public class AnalyzeScheduleParser {

    private static final Logger log = LogManager.getLogger(AnalyzeScheduleParser.class);

    public List<AnalyzeScheduleVo> parseScheduleFile(String filePath) {
        Path path = Path.of(filePath);
        log.info("분석 스케줄 파일 파싱 시작 - filePath: {}, absolutePath: {}", filePath, path.toAbsolutePath());

        Map<String, AnalyzeScheduleVo> scheduleByKey = new LinkedHashMap<>();

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                AnalyzeScheduleVo schedule = parseLine(lines.get(i), i + 1);
                if (schedule == null) {
                    continue;
                }

                // remove 후 put: LinkedHashMap에서 기존 항목을 지우고 재삽입하여
                // 삽입 순서를 파일 내 마지막 정의 기준으로 갱신 (중복 키는 마지막이 최종)
                String key = buildDedupKey(schedule.getServerId(), schedule.getSourceFilePath());
                scheduleByKey.remove(key);
                scheduleByKey.put(key, schedule);
            }
        } catch (IOException e) {
            log.error("분석 스케줄 파일 읽기 실패 - filePath: {}, absolutePath: {}, error: {}",
                    filePath, path.toAbsolutePath(), e.getMessage());
            throw new AnalyzeException("분석 스케줄 파일 읽기 실패: " + filePath, e);
        }

        List<AnalyzeScheduleVo> result = new ArrayList<>(scheduleByKey.values());
        log.info("분석 스케줄 파일 파싱 완료 - absolutePath: {}, 유효 스케줄 건수: {}", path.toAbsolutePath(), result.size());
        for (int i = 0; i < result.size(); i++) {
            AnalyzeScheduleVo s = result.get(i);
            log.info("  [{}] serverId: {}, sourceFilePath: {}, type: {}, day: {}, start: {}, interval: {}, end: {}",
                    i + 1,
                    s.getServerId(),
                    s.getSourceFilePath(),
                    s.getScheduleType(),
                    s.getDayOfWeek(),
                    s.getStartTime(),
                    s.getIntervalMinutes(),
                    s.getEndTime());
        }
        return result;
    }

    private AnalyzeScheduleVo parseLine(String line, int lineNumber) {
        if (line == null) {
            return null;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        List<String> tokens = extractBracketTokens(trimmed);
        if (tokens.size() != 2 && tokens.size() != 3) {
            log.warn("분석 스케줄 라인 포맷 오류로 무시 - lineNumber: {}, line: {}", lineNumber, trimmed);
            return null;
        }

        String serverId = tokens.get(0).trim();
        String sourceFilePath = tokens.size() == 3 ? tokens.get(1).trim() : null;
        String scheduleExpression = tokens.size() == 3 ? tokens.get(2).trim() : tokens.get(1).trim();

        if (serverId.isEmpty() || scheduleExpression.isEmpty()) {
            log.warn("분석 스케줄 라인 필수값 누락으로 무시 - lineNumber: {}, line: {}", lineNumber, trimmed);
            return null;
        }

        ScheduleParts parts = parseScheduleExpression(scheduleExpression);
        if (parts == null) {
            log.warn("분석주기기술 포맷 오류로 무시 - lineNumber: {}, schedule: {}", lineNumber, scheduleExpression);
            return null;
        }

        AnalyzeScheduleVo vo = new AnalyzeScheduleVo();
        vo.setServerId(serverId);
        vo.setSourceFilePath(sourceFilePath == null || sourceFilePath.isBlank() ? null : sourceFilePath);
        vo.setScheduleType(parts.type);
        vo.setDayOfWeek(parts.daySpec);
        vo.setStartTime(parts.startTime);
        vo.setIntervalMinutes(parts.intervalMinutes);
        vo.setEndTime(parts.endTime);
        return vo;
    }

    private List<String> extractBracketTokens(String text) {
        List<String> tokens = new ArrayList<>(3);
        int i = 0;
        while (i < text.length()) {
            int start = text.indexOf('[', i);
            if (start < 0) {
                break;
            }
            int end = text.indexOf(']', start + 1);
            if (end < 0) {
                break;
            }
            tokens.add(text.substring(start + 1, end));
            i = end + 1;
        }
        return tokens;
    }

    private ScheduleParts parseScheduleExpression(String scheduleExpression) {
        String[] parts = scheduleExpression.split("\\|", -1);
        if (parts.length == 0) {
            return null;
        }

        String type = parts[0].trim();
        if ("배치".equals(type)) {
            if (parts.length != 3) {
                return null;
            }
            String daySpec = parts[1].trim();
            String startTime = parts[2].trim();
            if (!isValidDaySpec(daySpec) || !isValidTimeHhmmss(startTime, true)) {
                return null;
            }
            return new ScheduleParts(type, daySpec, startTime, null, null);
        }

        if ("주기".equals(type)) {
            if (parts.length != 5) {
                return null;
            }

            String daySpec = parts[1].trim();
            String startTime = parts[2].trim();
            String intervalMinutesText = parts[3].trim();
            String endTime = parts[4].trim();

            if (!isValidDaySpec(daySpec) || !isValidTimeHhmmss(startTime, true) || !isValidIntervalMinutes(intervalMinutesText)
                    || !isValidTimeHhmmss(endTime, false)) {
                return null;
            }

            return new ScheduleParts(type, daySpec, startTime, Integer.parseInt(intervalMinutesText), endTime);
        }

        return null;
    }

    private boolean isValidDaySpec(String daySpec) {
        if (daySpec == null || daySpec.isEmpty()) {
            return false;
        }

        if (daySpec.contains(",") || daySpec.contains(" ")) {
            return false;
        }

        if ("*".equals(daySpec)) {
            return true;
        }

        if (daySpec.contains("-")) {
            String[] range = daySpec.split("-", -1);
            if (range.length != 2) {
                return false;
            }
            Integer start = parseDay(range[0]);
            Integer end = parseDay(range[1]);
            if (start == null || end == null) {
                return false;
            }
            return start <= end;
        }

        return parseDay(daySpec) != null;
    }

    private Integer parseDay(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.length() != 1) {
            return null;
        }
        char c = text.charAt(0);
        if (c < '0' || c > '6') {
            return null;
        }
        return c - '0';
    }

    private boolean isValidTimeHhmmss(String timeText, boolean isStartTime) {
        if (timeText == null || timeText.length() != 6) {
            return false;
        }

        int time;
        try {
            time = Integer.parseInt(timeText);
        } catch (NumberFormatException e) {
            return false;
        }

        int hh = time / 10000;
        int mm = (time / 100) % 100;
        int ss = time % 100;
        if (hh < 0 || hh > 23) {
            return false;
        }
        if (mm < 0 || mm > 59) {
            return false;
        }
        if (ss < 0 || ss > 59) {
            return false;
        }

        if (isStartTime) {
            return time >= 1;
        }
        return time <= 235959;
    }

    private boolean isValidIntervalMinutes(String minutesText) {
        if (minutesText == null || minutesText.isEmpty()) {
            return false;
        }
        try {
            int minutes = Integer.parseInt(minutesText);
            return minutes > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String buildDedupKey(String serverId, String sourceFilePath) {
        String filePart = sourceFilePath == null ? "" : sourceFilePath;
        return serverId + "::" + filePart;
    }

    private static class ScheduleParts {
        private final String type;
        private final String daySpec;
        private final String startTime;
        private final Integer intervalMinutes;
        private final String endTime;

        private ScheduleParts(String type, String daySpec, String startTime, Integer intervalMinutes, String endTime) {
            this.type = type;
            this.daySpec = daySpec;
            this.startTime = startTime;
            this.intervalMinutes = intervalMinutes;
            this.endTime = endTime;
        }
    }
}

