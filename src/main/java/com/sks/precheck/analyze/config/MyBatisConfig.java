package com.sks.precheck.analyze.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 설정
 *
 * <p>역할:
 * - @MapperScan으로 Mapper 인터페이스 자동 스캔
 * - TB_COLLECT_LOG/TB_ANALYZE_RESULT/TB_ANALYZE_HISTORY 접근용
 *
 * <p>매퍼 목록:
 * - CollectLogMapper: TB_COLLECT_LOG SELECT (읽기 전용)
 * - AnalyzeResultMapper: TB_ANALYZE_RESULT INSERT
 * - AnalyzeHistoryMapper: TB_ANALYZE_HISTORY INSERT/UPDATE
 *
 * <p>매핑 파일 위치:
 * - src/main/resources/mapper/*.xml
 * - CollectLogMapper.xml, AnalyzeResultMapper.xml, AnalyzeHistoryMapper.xml
 *
 * <p>DB 설정:
 * - 테스트: application-test.yml → PostgreSQL (로컬)\n * - 운영: application-prod.yml → Altibase (원격)
 */\n@Configuration
@MapperScan("com.sks.precheck.analyze.mapper")
public class MyBatisConfig {
    // MyBatis 커스텀 설정 (현재 기본값 사용)
}
