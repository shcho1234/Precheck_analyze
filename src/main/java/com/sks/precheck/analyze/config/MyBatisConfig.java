package com.sks.precheck.analyze.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.sks.precheck.analyze.mapper")
public class MyBatisConfig {
}
