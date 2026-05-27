package com.sks.precheck.analyze.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 설정
 * Mapper 스캔 위치 지정
 */
@Configuration
@MapperScan("com.sks.precheck.analyze.mapper")
public class MyBatisConfig {
    // MyBatis 커스텀 설정
}
