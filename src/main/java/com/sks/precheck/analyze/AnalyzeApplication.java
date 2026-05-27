package com.sks.precheck.analyze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * AnalyzeApplication - Spring Boot 애플리케이션 진입점
 *
 * <p>기능:
 * - SpringBoot 애플리케이션을 시작
 * - 스케줄 활성화를 위해 {@link EnableScheduling} 사용
 *
 * <p>실행 예:
 * <pre>
 * java -jar analyze.jar
 * </pre>
 */
public class AnalyzeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzeApplication.class, args);
	}

}
