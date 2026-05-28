package com.sks.precheck.analyze.config;

import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;
import com.sks.precheck.analyze.parser.AnalyzePolicyParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 분석 정책 로더 — @PostConstruct 1회 로딩, 이후 "serverId:logId" 키로 O(1) 조회
 *
 * 파일 위치: precheck.analyze.policy-file-path (yml 미설정 시 {user.home}/cfg/PreCheck_AnalyzePolicy.conf)
 * 파일이 없으면 빈 맵으로 초기화(에러 아님). 파일은 서버 재시작 전까지 재로딩 없음.
 */
@Component
public class PolicyLoader {

    private static final Logger log = LogManager.getLogger(PolicyLoader.class);

    private static final String DEFAULT_POLICY_FILE_RELATIVE_PATH = "/cfg/PreCheck_AnalyzePolicy.conf";

    private final AnalyzePolicyParser analyzePolicyParser;
    private final String policyFilePath;
    private Map<String, AnalyzePolicy> policyMap = new HashMap<>();

    public PolicyLoader(
            AnalyzePolicyParser analyzePolicyParser,
            @Value("${precheck.analyze.policy-file-path:}") String policyFilePath
    ) {
        this.analyzePolicyParser = analyzePolicyParser;
        this.policyFilePath = (policyFilePath == null || policyFilePath.isBlank())
                ? System.getProperty("user.home") + DEFAULT_POLICY_FILE_RELATIVE_PATH
                : policyFilePath;
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
        return Path.of(policyFilePath);
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
