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

@Configuration
public class DataSourceConfig {

    private static final Logger log = LogManager.getLogger(DataSourceConfig.class);

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
