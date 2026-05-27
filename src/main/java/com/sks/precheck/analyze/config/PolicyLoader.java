package com.sks.precheck.analyze.config;

import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.parser.AnalyzePolicyParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 분석 정책 로더
 *
 * <p>역할:
 * - 서버 시작 시 (@PostConstruct) PreCheck_AnalyzePolicy.conf 파일을 1회 로딩
 * - 정책들을 메모리에 HashMap으로 저장 (serverId + logId를 키로)
 * - 분석 중에 정책을 O(1) 시간에 조회 가능
 *
 * <p>정책 파일 위치:
 * - 기본: {user.home}/cfg/PreCheck_AnalyzePolicy.conf
 * - 예) C:\\Users\\username\\cfg\\PreCheck_AnalyzePolicy.conf (Windows)
 * - 예) /home/username/cfg/PreCheck_AnalyzePolicy.conf (Linux)
 *
 * <p>정책 파일 형식:
 * [serverId][logId][로그타입][타입별 파라미터...]
 *
 * <p>로딩 동작:
 * 1. UTF-8로 파일 읽기
 * 2. 각 라인을 AnalyzePolicyParser로 파싱
 * 3. 포맷 불일치 라인은 WARN 로그만 기록하고 스킵
 * 4. 파싱 성공한 정책을 HashMap에 저장 (serverId:logId 키로)
 * 5. 로딩 완료 시 정책 건수 INFO 로그
 *
 * <p>주의:
 * - 파일이 없으면 빈 HashMap으로 초기화 (에러 아님)
 * - 분석 중 정책 미등록 LOG_ID는 LEVEL_UNANALYZED로 저장
 * - 정책 파일은 서버 시작 후 변경되지 않음 (재로딩 없음)
 *
 * @see AnalyzePolicyParser 정책 파일 라인 파싱\n */
@Component
public class PolicyLoader {

    private static final Logger log = LogManager.getLogger(PolicyLoader.class);

    private final AnalyzePolicyParser analyzePolicyParser;
    private Map<String, AnalyzePolicy> policyMap = new HashMap<>();

    public PolicyLoader(AnalyzePolicyParser analyzePolicyParser) {
        this.analyzePolicyParser = analyzePolicyParser;
    }

    @PostConstruct
    public void load() {
        Path path = resolvePolicyPath();
        if (!Files.exists(path)) {
            log.warn("분석 정책 파일이 존재하지 않음: {}", path.toAbsolutePath());
            policyMap = new HashMap<>();
            return;
        }

        Map<String, AnalyzePolicy> loaded = new HashMap<>();
        int lineNumber = 0;

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                AnalyzePolicy policy;
                try {
                    policy = analyzePolicyParser.parse(line);
                } catch (Exception e) {
                    log.warn("분석 정책 파싱 실패 - line: {}, text: {}", lineNumber, line);
                    continue;
                }

                if (policy == null) {
                    if (shouldWarnInvalidLine(line)) {
                        log.warn("분석 정책 포맷 불일치 - line: {}, text: {}", lineNumber, line);
                    }
                    continue;
                }

                String key = buildPolicyKey(policy.getServerId(), policy.getLogId());
                loaded.put(key, policy);
            }
        } catch (IOException e) {
            log.error("분석 정책 파일 로딩 실패: {}", path.toAbsolutePath(), e);
            policyMap = new HashMap<>();
            return;
        }

        policyMap = loaded;
        log.info("분석 정책 로딩 완료 - count: {}, path: {}", policyMap.size(), path.toAbsolutePath());
    }

    public AnalyzePolicy findPolicy(String serverId, String logId) {
        return policyMap.get(buildPolicyKey(serverId, logId));
    }

    public Map<String, AnalyzePolicy> getPolicyMap() {
        return Collections.unmodifiableMap(policyMap);
    }

    private Path resolvePolicyPath() {
        return Paths.get(System.getProperty("user.home"), "cfg", "PreCheck_AnalyzePolicy.conf");
    }

    private String buildPolicyKey(String serverId, String logId) {
        return serverId + ":" + logId;
    }

    private boolean shouldWarnInvalidLine(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        return !trimmed.isEmpty() && !trimmed.startsWith("#");
    }
}
