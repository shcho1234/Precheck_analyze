package com.sks.precheck.analyze.parser;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.domain.policy.DatePolicy;
import com.sks.precheck.analyze.domain.policy.ExistencePolicy;
import com.sks.precheck.analyze.domain.policy.InfoPolicy;
import com.sks.precheck.analyze.domain.policy.NumericPolicy;
import com.sks.precheck.analyze.domain.policy.PhrasePolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 분석 정책 파일 파서
 *
 * <p>역할: PreCheck_AnalyzePolicy.conf 파일의 한 라인을 파싱하여 정책 객체 반환
 *
 * <p>정책 파일 형식 (PreCheck_AnalyzePolicy.conf):
 * [serverId][logId][로그타입][타입별 파라미터...]
 *
 * <p>타입별 포맷:
 * - 문구: [SRV001][ERROR_LOG][문구][keyword1,keyword2,keyword3]
 * - 수치: [SRV001][DISK_USAGE][수치][>][80][20]
 * - 날짜: [SRV001][BACKUP_DATE][날짜]
 * - 존재: [SRV001][FILE_CHECK][존재]
 * - 정보: [SRV001][DAILY_LOG][정보]
 *
 * <p>파싱 규칙:
 * - 공백 라인이나 #로 시작하는 주석은 무시
 * - 포맷이 맞지 않으면 null 반환 (에러 발생 안함)
 * - serverId/logId/logType 중 하나라도 없으면 null 반환
 * - 수치형 포맷이 6개가 아니면 null 반환 (threshold/warningRatio가 숫자가 아니면 null)
 *
 * @see PolicyLoader 서버 시작 시 정책 파일 전체 로딩
 */\n@Component
public class AnalyzePolicyParser {

    private static final Logger log = LogManager.getLogger(AnalyzePolicyParser.class);

    public AnalyzePolicy parse(String line) {
        if (line == null) {
            return null;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        List<String> tokens = extractBracketTokens(trimmed);
        if (tokens == null || tokens.size() < 3) {
            return null;
        }

        String serverId = tokens.get(0);
        String logId = tokens.get(1);
        String logType = tokens.get(2);

        if (isBlank(serverId) || isBlank(logId) || isBlank(logType)) {
            return null;
        }

        if (AnalyzeConstants.LOG_TYPE_TEXT.equals(logType)) {
            return parsePhrasePolicy(serverId, logId, tokens);
        }
        if (AnalyzeConstants.LOG_TYPE_NUMERIC.equals(logType)) {
            return parseNumericPolicy(serverId, logId, tokens);
        }
        if (AnalyzeConstants.LOG_TYPE_DATE.equals(logType)) {
            return parseDatePolicy(serverId, logId, tokens);
        }
        if (AnalyzeConstants.LOG_TYPE_EXIST.equals(logType)) {
            return parseExistencePolicy(serverId, logId, tokens);
        }
        if (AnalyzeConstants.LOG_TYPE_INFO.equals(logType)) {
            return parseInfoPolicy(serverId, logId, tokens);
        }

        return null;
    }

    private PhrasePolicy parsePhrasePolicy(String serverId, String logId, List<String> tokens) {
        if (tokens.size() != 4) {
            return null;
        }

        PhrasePolicy policy = new PhrasePolicy();
        policy.setServerId(serverId);
        policy.setLogId(logId);
        policy.setErrorKeywords(splitKeywords(tokens.get(3)));
        return policy;
    }

    private NumericPolicy parseNumericPolicy(String serverId, String logId, List<String> tokens) {
        if (tokens.size() != 6) {
            return null;
        }

        String operator = tokens.get(3);
        String thresholdText = tokens.get(4);
        String warningRatioText = tokens.get(5);

        if (isBlank(operator) || isBlank(thresholdText) || isBlank(warningRatioText)) {
            return null;
        }

        BigDecimal threshold;
        BigDecimal warningRatio;
        try {
            threshold = new BigDecimal(thresholdText.trim());
            warningRatio = new BigDecimal(warningRatioText.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        NumericPolicy policy = new NumericPolicy();
        policy.setServerId(serverId);
        policy.setLogId(logId);
        policy.setOperator(operator.trim());
        policy.setThreshold(threshold);
        policy.setWarningRatio(warningRatio);
        return policy;
    }

    private DatePolicy parseDatePolicy(String serverId, String logId, List<String> tokens) {
        if (tokens.size() != 3) {
            return null;
        }

        DatePolicy policy = new DatePolicy();
        policy.setServerId(serverId);
        policy.setLogId(logId);
        return policy;
    }

    private ExistencePolicy parseExistencePolicy(String serverId, String logId, List<String> tokens) {
        if (tokens.size() != 3) {
            return null;
        }

        ExistencePolicy policy = new ExistencePolicy();
        policy.setServerId(serverId);
        policy.setLogId(logId);
        return policy;
    }

    private InfoPolicy parseInfoPolicy(String serverId, String logId, List<String> tokens) {
        if (tokens.size() != 3) {
            return null;
        }

        InfoPolicy policy = new InfoPolicy();
        policy.setServerId(serverId);
        policy.setLogId(logId);
        return policy;
    }

    private List<String> extractBracketTokens(String text) {
        int idx = 0;
        List<String> tokens = new ArrayList<>();

        while (idx < text.length()) {
            int open = text.indexOf('[', idx);
            if (open < 0) {
                break;
            }
            int close = text.indexOf(']', open + 1);
            if (close < 0) {
                return null;
            }
            tokens.add(text.substring(open + 1, close).trim());
            idx = close + 1;
        }

        if (tokens.isEmpty()) {
            return null;
        }
        return tokens;
    }

    private List<String> splitKeywords(String keywordText) {
        List<String> keywords = new ArrayList<>();
        if (keywordText == null) {
            return keywords;
        }

        String[] parts = keywordText.split(",");
        for (String part : parts) {
            String keyword = part.trim();
            if (!keyword.isEmpty()) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
