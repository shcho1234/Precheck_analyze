# PreCheck 분석 서버 - AI 코드 생성 Rule
# 이 파일은 Trae가 분석 서버 코드를 생성·수정할 때 반드시 따르는 규칙이다.
# Context 파일에 명세 상세 내용이 있으니 모르는 내용은 Context를 참고한다.

---

## 1. 기술 스택 (변경 금지)

### 사용 필수
| 구분 | 기술 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x |
| 빌드 | Gradle |
| DB 접근 | MyBatis |
| 스케줄 | Spring Scheduler (@Scheduled) |
| 재시도 | Spring Retry (@Retryable) |
| 로그 | Log4j2 |
| 수치 비교 | BigDecimal |
| 테스트 | JUnit 5 |

### 사용 금지
- JPA / Hibernate / Spring Data JPA
- Quartz Scheduler
- Logback
- SSHJ (분석 서버는 SFTP 사용 금지, TB_COLLECT_LOG DB 조회만 사용)
- float / double (수치 계산 전용)

---

## 2. DB 타입 규칙 (Altibase + PostgreSQL 공통)

### Java ↔ DB 타입 매핑
| DB 타입 | Java 타입 | 비고 |
|---|---|---|
| NUMERIC(19,0) | Long | PK 등 정수 |
| NUMERIC(18,6) | BigDecimal | 수치형 로그값, 임계치 |
| NUMERIC(5,2) | BigDecimal | 경고근접비율 |
| CHAR(1) | String | "Y" 또는 "N" 만 허용 |
| VARCHAR(n) | String | 최대 VARCHAR(4000) |
| TIMESTAMP | LocalDateTime | |

### 금지 타입
| 금지 | 대체 |
|---|---|
| BOOLEAN | CHAR(1) ('Y'/'N') |
| TEXT | VARCHAR(4000) 이하 |
| BIGINT | NUMERIC(19,0) |
| SERIAL / AUTO_INCREMENT | SEQUENCE 객체 |
| CLOB | VARCHAR(4000) 이하 |

### 기타 DB 규칙
- PK는 SEQUENCE 객체로 생성, MyBatis에서 nextval 호출 후 파라미터로 전달
- DB 레벨 FK 제약 생성 금지, 정합성은 애플리케이션에서 관리
- TB_COLLECT_LOG는 읽기(SELECT)만 허용, INSERT/UPDATE/DELETE 금지

---

## 3. 분석 서버 역할 경계 (혼용 금지)

### 분석 서버가 하는 일
- TB_COLLECT_LOG에서 수집된 정규화 로그를 조회
- 분석 정책 파일(PreCheck_AnalyzePolicy.conf)을 서버 시작 시 1회 로딩
- 분석 스케줄 파일(PreCheck_AnalyzeLogs_Schedule.conf)에 따라 분석 실행
- 로그 타입별 분석 수행 (문구/수치/날짜/존재/정보)
- 분석 결과 TB_ANALYZE_RESULT 저장
- 분석 실행 이력 TB_ANALYZE_HISTORY 저장

### 분석 서버가 절대 하면 안 되는 일
- SFTP로 레거시 서버에 직접 접근 금지
- SMS / 통보 로직 포함 금지
- TB_COLLECT_LOG INSERT/UPDATE/DELETE 금지
- TB_COLLECT_HISTORY / TB_COLLECT_EXCLUDE 접근 금지
- TB_ANALYZE_RESULT NOTIFY_YN 컬럼 UPDATE 금지
  (NOTIFY_YN 처리는 결과 통보 서버 전용)

---

## 4. 패키지 구조 (이 구조를 준용하되 필요하면 생성 가능)
com.sks.precheck.analyze
├── AnalyzeApplication.java
├── common/
│   ├── constants/AnalyzeConstants.java
│   ├── exception/AnalyzeException.java
│   └── util/DateUtil.java
├── config/
│   ├── DataSourceConfig.java
│   ├── MyBatisConfig.java
│   ├── RetryConfig.java
│   └── PolicyLoader.java              ← 분석 정책 파일 로딩 (서버 시작 시 1회)
├── domain/
│   ├── CollectLog.java                ← TB_COLLECT_LOG 조회용 DTO (읽기 전용)
│   ├── AnalyzeResult.java             ← TB_ANALYZE_RESULT INSERT용 DTO
│   ├── AnalyzeHistory.java            ← TB_ANALYZE_HISTORY INSERT/UPDATE용 DTO
│   └── policy/
│       ├── AnalyzePolicy.java         ← 정책 공통 인터페이스
│       ├── PhrasePolicy.java          ← 문구형 정책 (에러 키워드 목록)
│       ├── NumericPolicy.java         ← 수치형 정책 (연산자, 임계치, 경고근접비율)
│       ├── DatePolicy.java            ← 날짜형 정책
│       ├── ExistencePolicy.java       ← 존재형 정책
│       └── InfoPolicy.java            ← 정보형 정책
├── mapper/
│   ├── CollectLogMapper.java          ← TB_COLLECT_LOG SELECT 전용
│   ├── AnalyzeResultMapper.java       ← TB_ANALYZE_RESULT INSERT
│   └── AnalyzeHistoryMapper.java      ← TB_ANALYZE_HISTORY INSERT/UPDATE
├── analyzer/
│   ├── LogAnalyzer.java               ← 분석기 공통 인터페이스
│   ├── PhraseAnalyzer.java            ← 문구형 분석
│   ├── NumericAnalyzer.java           ← 수치형 분석 (BigDecimal 필수)
│   ├── DateAnalyzer.java              ← 날짜형 분석
│   ├── ExistenceAnalyzer.java         ← 존재형 분석
│   └── InfoAnalyzer.java              ← 정보형 분석
├── parser/
│   ├── AnalyzeScheduleParser.java     ← 분석 스케줄 .conf 파싱
│   └── AnalyzePolicyParser.java       ← 분석 정책 .conf 파싱
├── scheduler/
│   └── AnalyzeScheduler.java
├── service/
│   ├── AnalyzeService.java            ← 분석 진입점, 이력 선등록 후 RetryService 위임
│   └── AnalyzeRetryService.java       ← @Retryable/@Recover 실제 분석 처리
└── vo/
└── AnalyzeScheduleVo.java
resources/
├── application.yml
├── application-local.yml
├── application-test.yml
├── application-prod.yml
├── log4j2-spring.xml
└── mapper/
├── CollectLogMapper.xml
├── AnalyzeResultMapper.xml
└── AnalyzeHistoryMapper.xml

아래 파일은 프로잭트 루트 밑에 만든다.
policy_sample/PreCheck_AnalyzePolicy.conf
schedule_sample/PreCheck_AnalyzeLogs_Schedule.conf
---

## 5. 네이밍 규칙

### 클래스
| 종류 | 패턴 | 예시 |
|---|---|---|
| 서비스 | {기능}Service | AnalyzeService, AnalyzeRetryService |
| 스케줄러 | {기능}Scheduler | AnalyzeScheduler |
| 파서 | {대상}Parser | AnalyzeScheduleParser, AnalyzePolicyParser |
| 분석기 | {타입}Analyzer | PhraseAnalyzer, NumericAnalyzer |
| Mapper | {테이블}Mapper | CollectLogMapper, AnalyzeResultMapper |
| 도메인 DTO | 테이블명 축약 | CollectLog, AnalyzeResult, AnalyzeHistory |
| 정책 DTO | {타입}Policy | PhrasePolicy, NumericPolicy |
| VO | {기능}Vo | AnalyzeScheduleVo |
| 예외 | {서버}Exception | AnalyzeException |
| 상수 | {서버}Constants | AnalyzeConstants |

### 메서드
| 동작 | 접두사 | 예시 |
|---|---|---|
| DB 단건 조회 | find | findLastAnalyzeLogId |
| DB 목록 조회 | findAll / findBy~ | findAllByServerId |
| DB 저장 | insert | insertAnalyzeResult |
| DB 수정 | update | updateAnalyzeHistory |
| 파싱 | parse | parsePolicyFile, parseScheduleFile |
| 검증 | is / has / validate | hasPolicy, isToday |
| 분석 실행 | analyze | analyzeBatch, analyzePeriodic |
| 정책 조회 | find / get | findPolicy, getAnalyzer |

### 변수 / 상수
```java
// 변수: 의미와 단위가 명확하게
BigDecimal logValue       = collectLog.getLogValue();    // 수치형 로그값
BigDecimal thresholdValue = numericPolicy.getThreshold(); // 임계치
Long lastAnalyzeLogId     = history.getLastAnalyzeLogId(); // 마지막 분석 로그 ID

// 상수: AnalyzeConstants에 정의, 매직 넘버 사용 금지
public static final int    MAX_RETRY_COUNT              = 3;
public static final long   RETRY_DELAY_MILLISECONDS     = 300_000L;        // 5분
public static final String ANALYZE_DATE_FORMAT          = "yyyyMMdd";
public static final String LOG_TIMESTAMP_FORMAT         = "yyyy/MM/dd HH:mm:ss.SSS";
public static final String LOG_DATE_FORMAT              = "yyyy/MM/dd";

// 로그 타입 상수
public static final String LOG_TYPE_TEXT     = "문구";
public static final String LOG_TYPE_INFO     = "정보";
public static final String LOG_TYPE_DATE     = "날짜";
public static final String LOG_TYPE_NUMERIC  = "수치";
public static final String LOG_TYPE_EXIST    = "존재";

// 분석 레벨 상수
public static final String LEVEL_NORMAL      = "정상";
public static final String LEVEL_WARNING     = "경고";
public static final String LEVEL_ERROR       = "에러";
public static final String LEVEL_INFO        = "정보";
public static final String LEVEL_UNANALYZED  = "미분석";

// 분석 이력 상태 상수
public static final String STATUS_SUCCESS    = "SUCCESS";
public static final String STATUS_FAIL       = "FAIL";
public static final String STATUS_PARTIAL    = "PARTIAL";

// YN 상수
public static final String YN_YES = "Y";
public static final String YN_NO  = "N";

// 정책 키 구분자
public static final String POLICY_KEY_SEPARATOR = ":";
// 정책 키 생성: serverId + ":" + logId
```

---

## 6. 클래스 내부 선언 순서

```java
public class XxxService {

    // 1. 로거
    private static final Logger log = LogManager.getLogger(XxxService.class);

    // 2. 상수 (클래스 내부 전용 상수만, 공통은 AnalyzeConstants)

    // 3. 의존성 주입 필드 (final)
    private final AnalyzeResultMapper analyzeResultMapper;

    // 4. 생성자 (의존성 주입)
    public XxxService(AnalyzeResultMapper analyzeResultMapper) {
        this.analyzeResultMapper = analyzeResultMapper;
    }

    // 5. public 메서드

    // 6. private 메서드 (내부 헬퍼)

}
```

---

## 7. 주석 규칙

### 언어
- **모든 주석은 한국어**로 작성
- 코드(클래스명·메서드명·변수명)는 영어
- MyBatis, SEQUENCE, BigDecimal 등 기술 용어는 영어 허용

### Javadoc 작성 대상 (보통 엄격도)
| 작성 필수 | 생략 가능 |
|---|---|
| 모든 public 클래스 | getter / setter |
| 비즈니스 로직 포함 public 메서드 | 기본 생성자 |
| 복잡한 private 메서드 (30줄↑ 또는 분기 3개↑) | 단순 위임 메서드 |
| 커스텀 예외 클래스 | 자명한 상수 |
| 분석기(Analyzer) 구현 클래스 전체 | |

### Javadoc 형식
```java
/**
 * [한 줄 요약 - 동사로 시작]
 *
 * [상세 설명 - 비즈니스 규칙, 주의사항이 있을 때만]
 *
 * @param 파라미터명 설명
 * @return 반환값 설명 (void면 생략)
 * @throws 예외클래스 발생 조건
 */
```

### 인라인 주석 - 달아야 하는 경우
```java
// ✅ 분석 정책 키 생성 규칙
// 정책 키는 "서버구분:LOG_ID" 형식으로 구성한다
String policyKey = serverId + AnalyzeConstants.POLICY_KEY_SEPARATOR + logId;

// ✅ 정책 미등록 LOG_ID 처리 규칙
// 분석 정책 파일에 등록되지 않은 LOG_ID는 '미분석' 레벨로 저장하고
// 통보 대상에서 제외된다 (분석 정책 정의서 3번 참고)
if (policy == null) {
    saveUnanalyzed(collectLog);
    return;
}

// ✅ 중복 분석 방지 핵심 로직
// 이전 분석의 마지막 COLLECT_LOG_ID 이후부터만 조회하여 중복 분석을 방지한다
Long lastAnalyzeLogId = findLastAnalyzeLogId(serverId, filePath);

// ✅ BigDecimal 사용 이유
// 수치형 임계치 비교 시 float/double의 부동소수점 오차를 방지하기 위해
// 반드시 BigDecimal을 사용한다
BigDecimal result = logValue.compareTo(threshold);

// ❌ 자명한 코드에 주석 금지
int count = 0; // count를 0으로 초기화  ← 이런 주석은 작성하지 않는다
```

---

## 8. 예외 처리 & 로그 레벨

| 레벨 | 사용 기준 |
|---|---|
| ERROR | DB 오류, 분석 실패, 재시도 3회 모두 실패 |
| WARN | 정책 미등록 LOG_ID, 포맷 불일치, 미분석 처리 |
| INFO | 분석 시작·완료, 건수 등 주요 결과 |
| DEBUG | 건별 분석 상세 처리 (운영 OFF) |

```java
// ERROR: 시스템 이상
log.error("분석 결과 DB 저장 실패 - 서버: {}, LOG_ID: {}", serverId, logId, e);
throw new AnalyzeException("분석 결과 저장 실패: " + logId, e);

// WARN: 정책 미등록
log.warn("분석 정책 미등록 LOG_ID - 서버: {}, LOG_ID: {}, 미분석 처리",
         serverId, logId);

// INFO: 분석 완료
log.info("분석 완료 - 서버: {}, 총: {}건, 에러: {}건, 경고: {}건",
         serverId, totalCount, errorCount, warningCount);
```

---

## 9. 분석기(Analyzer) 구현 규칙

### 공통 원칙
- 모든 Analyzer는 `LogAnalyzer` 인터페이스를 구현한다
- Analyzer는 순수 분석 로직만 포함, DB 접근 금지
- 분석 결과는 항상 `AnalyzeResult` 객체로 반환한다
- `ANALYZE_MESSAGE`는 아래 형식을 정확히 준수한다

### ANALYZE_MESSAGE 작성 형식
// 문구형
정상: "[정상][{LOG_ID}] {LOG_CONTENT}"
에러: "[에러][{LOG_ID}] {LOG_CONTENT} (키워드: {매칭된 키워드})"

// 수치형
정상: "[정상][{LOG_ID}] {LOG_CONTENT} {LOG_VALUE} {수치} {연산자} {임계치}(임계치)"
경고: "[경고][{LOG_ID}] {LOG_CONTENT} {LOG_VALUE} {수치} {연산자} {임계치}(임계치 대비 {비율}% 근접)"
에러: "[에러][{LOG_ID}] {LOG_CONTENT} {LOG_VALUE} 조건불만족 {수치} {연산자} {임계치}(임계치)"

// 날짜형
정상: "[정상][{LOG_ID}] {LOG_CONTENT} (날짜 일치)"
에러: "[에러][{LOG_ID}] {LOG_CONTENT} (날짜 불일치: 기대={오늘날짜}, 실제={로그날짜})"

// 존재형
에러: "[에러][{LOG_ID}] {LOG_CONTENT}"

// 정보형
정보: "[정보][{LOG_ID}] {LOG_CONTENT}"

// 미분석
미분석: "[미분석][{LOG_ID}] 미분석"
```

### 수치형 분석 경고 판정 로직 (NumericAnalyzer 전용)
연산자가 < 또는 <= 인 경우 (값이 작아야 정상):
정상: logValue < threshold (또는 <=)
경고: logValue >= (threshold - threshold * warningRatio / 100)
에러: logValue >= threshold
연산자가 > 또는 >= 인 경우 (값이 커야 정상):
정상: logValue > threshold (또는 >=)
경고: logValue <= (threshold + threshold * warningRatio / 100)
에러: logValue <= threshold
모든 수치 비교는 반드시 BigDecimal.compareTo() 사용
Float / Double 연산 절대 금지

---

## 10. MyBatis XML 주석 형식

```xml
<!--
    XxxMapper.xml
    TB_XXX 테이블 CRUD
    INSERT 주체: 로그 분석 서버 / SELECT 주체: ~
-->
<mapper namespace="com.sks.precheck.analyze.mapper.XxxMapper">

    <!-- [목적 한 줄 설명] -->
    <!-- [주의사항이 있으면 추가] -->
    <select id="findForAnalyze" ...> ... </select>

</mapper>
```

---

## 11. 분석 정책 파일 규칙

### 파일 위치
루트디렉토리 하위 policy_sample/PreCheck_AnalyzePolicy.conf

### 정책 키 생성 규칙
```java
// 정책은 "서버구분:LOG_ID" 조합으로 Map에 저장
// 동일 키 중복 시 마지막 줄이 적용됨
Map<String, AnalyzePolicy> policyMap = new HashMap<>();
String key = serverId + ":" + logId;   // 예) "dlprem01-테스트개발:DISK_HOME"
policyMap.put(key, policy);
```

### 정책 로딩 규칙
- `@PostConstruct`로 서버 시작 시 1회만 로딩
- 포맷 불일치 줄은 WARN 로그 후 무시 (서버 중단 금지)
- `#`으로 시작하는 줄은 주석으로 skip
- 운영 중 정책 변경 시 서버 재기동 필요 (동적 리로드 미지원)

---

## 12. 이력 선등록 패턴 (collect 서버와 동일)

```java
// AnalyzeService.analyze() 에서:
// 1. SEQUENCE로 PK 채번
Long historyId = sequenceHelper.nextVal("SEQ_ANALYZE_HISTORY");

// 2. STATUS=FAIL, failReason=IN_PROGRESS로 이력 선등록
//    (서버 다운 시에도 실행 이력이 남도록)
AnalyzeHistory history = new AnalyzeHistory();
history.setAnalyzeHistoryId(historyId);
history.setAnalyzeStatus(AnalyzeConstants.STATUS_FAIL);
history.setFailReason("IN_PROGRESS");
analyzeHistoryMapper.insert(history);

// 3. AnalyzeRetryService에 위임 → 내부에서 SUCCESS/FAIL로 UPDATE
analyzeRetryService.analyze(scheduleVo, historyId);
```

---

## 13. TODO 형식

```java
// TODO: [작업 내용] - [이유 또는 참고 파일]
// TODO: SEQUENCE nextval 조회 추가 - 06_analyze_db.md 참고
// TODO: 경고 판정 로직 단위 테스트 추가 - NumericAnalyzer 경고 영역 계산 검증 필요
```

---

## 14. AI 코드 생성 행동 규칙

- 생성 완료 후 아래 형식으로 반드시 출력한다
✅ 생성 완료

파일: [파일명]
역할: [한 줄 요약]
다음 단계: [다음에 만들 파일 또는 확인 사항]

- 명세(Context 파일)와 충돌이 생기면 **코드 작성 전에 먼저 질문한다**
- 패키지 구조·클래스 분리 등 구조 변경이 필요하면 **먼저 설명하고 승인을 받는다**
- 테스트 코드는 요청 시에만 생성한다
- 모르는 내용은 추측하지 말고 Context 파일을 참고하거나 질문한다
- **TB_COLLECT_LOG는 SELECT만 허용** - INSERT/UPDATE/DELETE 코드 생성 금지
- **수치 비교는 반드시 BigDecimal** - float/double 코드 생성 금지
- **분석 정책 조회 키는 반드시 "서버구분:LOG_ID" 형식** 준수
