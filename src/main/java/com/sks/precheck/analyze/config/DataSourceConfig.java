package com.sks.precheck.analyze.config;

import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 데이터소스 설정
 *
 * <p>역할: Spring 프로파일(test/prod)에 따라 다른 DataSource 생성
 * - test: 로컬 PostgreSQL 데이터소스
 * - prod: Altibase 데이터소스
 *
 * <p>설정 출처: application-{profile}.yml
 * - application.yml (기본)
 * - application-test.yml (테스트 환경)
 * - application-prod.yml (운영 환경)
 *
 * <p>실행:
 * 1. 테스트: mvn test 또는 -Dspring.profiles.active=test
 * 2. 운영: -Dspring.profiles.active=prod
 *
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceProperties DataSource 속성
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Profile("test")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties testDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Profile("test")
    public DataSource testDataSource(DataSourceProperties testDataSourceProperties) {
        return testDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Profile("prod")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties prodDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Profile("prod")
    public DataSource dataSource(DataSourceProperties prodDataSourceProperties) {
        return prodDataSourceProperties.initializeDataSourceBuilder().build();
    }
}
