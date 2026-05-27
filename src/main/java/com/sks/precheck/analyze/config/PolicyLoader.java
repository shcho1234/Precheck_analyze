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
