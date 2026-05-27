package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.DatePolicy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 날짜형 로그 분석기
 *
 * <p>명세서 - 분석 방식:
 * [날짜] : 로그내용 중 yyyy/MM/dd 형식의 날짜가 오늘과 같다면 정상, 아니면 에러
 *
 * <p>분석 결과:
 * - LEVEL_NORMAL: 로그에서 파싱한 날짜가 오늘 날짜와 일치
 * - LEVEL_ERROR: 로그에서 날짜를 파싱할 수 없거나 날짜가 오늘과 다름
 *
 * <p>예시:
 * - 오늘: 2026-05-27
 * - 로그: "2026/05/27 12:00:00 처리 완료" → LEVEL_NORMAL (2026/05/27 = 오늘)
 * - 로그: "2026/05/26 12:00:00 처리 완료" → LEVEL_ERROR (2026/05/26 ≠ 오늘)
 * - 로그: "처리 완료" → LEVEL_ERROR (날짜 파싱 불가)
 *
 * <p>구현: 로그 내용에서 정규식으로 yyyy/MM/dd 패턴 검색 후 파싱
 *
 * @see LogAnalyzer 분석기 인터페이스
 * @see DatePolicy 날짜형 정책 DTO
 */\n@Component
public class DateAnalyzer implements LogAnalyzer {

    private static final Logger log = LogManager.getLogger(DateAnalyzer.class);

    // 로그 내용에서 날짜를 파싱할 때 사용 (yyyy/MM/dd 형식)
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");

    @Override
    public AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy) {
        if (!(policy instanceof DatePolicy)) {
            throw new AnalyzeException("날짜형 정책이 아니다: " + policy);
        }

        String today = LocalDate.now().format(LOG_DATE_FORMATTER);
        List<String> dates = extractDates(log.getLogContent());

        String mismatched = findFirstMismatch(dates, today);
        String level = mismatched == null ? AnalyzeConstants.LEVEL_NORMAL : AnalyzeConstants.LEVEL_ERROR;

        AnalyzeResult result = baseResult(log);
        result.setAnalyzeLevel(level);
        result.setAnalyzeMessage(buildMessage(level, log.getLogId(), log.getLogContent(), today, mismatched));
        return result;
    }

    private AnalyzeResult baseResult(CollectLog log) {
        AnalyzeResult result = new AnalyzeResult();
        result.setCollectLogId(log.getCollectLogId());
        result.setServerId(log.getServerId());
        result.setServerIp(log.getServerIp());
        result.setLogType(log.getLogType());
        result.setLogId(log.getLogId());
        result.setLogTimestamp(log.getLogTimestamp());
        result.setLogContent(log.getLogContent());
        result.setLogValue(log.getLogValue());
        return result;
    }

    private List<String> extractDates(String content) {
        List<String> dates = new ArrayList<>();
        if (content == null) {
            return dates;
        }

        Matcher matcher = DATE_PATTERN.matcher(content);
        while (matcher.find()) {
            dates.add(matcher.group());
        }
        return dates;
    }

    private String findFirstMismatch(List<String> dates, String today) {
        if (dates == null || dates.isEmpty()) {
            return "없음";
        }
        for (String date : dates) {
            if (date != null && !date.equals(today)) {
                return date;
            }
        }
        return null;
    }

    private String buildMessage(String level, String logId, String content, String today, String mismatched) {
        if (AnalyzeConstants.LEVEL_ERROR.equals(level)) {
            return "[" + level + "][" + logId + "] " + content + " (날짜 불일치: 기대=" + today + ", 실제=" + mismatched + ")";
        }
        return "[" + level + "][" + logId + "] " + content + " (오늘 날짜 일치)";
    }
}
