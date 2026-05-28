# PreCheck 분석 서버 소스 코드 읽기 가이드

## 1. 서버 역할

수집 서버(collect)가 DB에 저장한 로그(`TB_COLLECT_LOG`)를 읽어 분석 정책에 따라
정상/경고/에러/정보/미분석 결과를 판정하고 `TB_ANALYZE_RESULT`에 저장하는 서버.

```
[수집 서버] → TB_COLLECT_LOG → [분석 서버] → TB_ANALYZE_RESULT
                                             → TB_ANALYZE_HISTORY
```

---

## 2. 프로젝트 디렉토리 구조

```
src/main/java/com/sks/precheck/analyze/
│
├── AnalyzeApplication.java          # Spring Boot 진입점 (@EnableScheduling)
│
├── scheduler/
│   └── AnalyzeScheduler.java        # 60초마다 실행, 스케줄 파일 해석 → 분석 트리거
│
├── service/
│   ├── AnalyzeService.java          # 분석 진입점: 이력 선등록 후 RetryService 위임
│   └── AnalyzeRetryService.java     # 실제 분석 수행 (@Retryable 5분 간격 3회 재시도)
│
├── analyzer/                        # [Strategy Pattern] 로그 타입별 분석 구현체
│   ├── LogAnalyzer.java             #   └─ 인터페이스
│   ├── PhraseAnalyzer.java          #   └─ 문구형: 키워드 포함 여부
│   ├── NumericAnalyzer.java         #   └─ 수치형: 임계치 비교 + 경고 구간
│   ├── DateAnalyzer.java            #   └─ 날짜형: 오늘 날짜 일치 여부
│   ├── ExistenceAnalyzer.java       #   └─ 존재형: 로그 존재 자체가 에러
│   └── InfoAnalyzer.java            #   └─ 정보형: 분석 없이 LEVEL_INFO 저장
│
├── domain/
│   ├── CollectLog.java              # TB_COLLECT_LOG 조회용 DTO
│   ├── AnalyzeResult.java           # TB_ANALYZE_RESULT 저장용 DTO
│   ├── AnalyzeHistory.java          # TB_ANALYZE_HISTORY 저장용 DTO
│   └── policy/                      # 분석 정책 도메인
│       ├── AnalyzePolicy.java       #   └─ 인터페이스 (serverId, logId, logType)
│       ├── PhrasePolicy.java        #   └─ 문구형: errorKeywords 목록
│       ├── NumericPolicy.java       #   └─ 수치형: operator, threshold, warningRatio
│       ├── DatePolicy.java          #   └─ 날짜형
│       ├── ExistencePolicy.java     #   └─ 존재형
│       └── InfoPolicy.java          #   └─ 정보형
│
├── config/
│   ├── PolicyLoader.java            # 서버 시작 시 정책 파일 1회 로딩 → HashMap
│   ├── DataSourceConfig.java        # 프로파일별 DataSource (test=PostgreSQL, prod=Altibase)
│   ├── MyBatisConfig.java           # @MapperScan
│   └── RetryConfig.java             # @EnableRetry 활성화
│
├── parser/
│   ├── AnalyzePolicyParser.java     # 정책 파일 한 라인 파싱 → AnalyzePolicy 객체
│   └── AnalyzeScheduleParser.java   # 스케줄 파일 전체 파싱 → AnalyzeScheduleVo 목록
│
├── mapper/
│   ├── CollectLogMapper.java        # TB_COLLECT_LOG SELECT
│   ├── AnalyzeResultMapper.java     # TB_ANALYZE_RESULT INSERT
│   └── AnalyzeHistoryMapper.java    # TB_ANALYZE_HISTORY INSERT/UPDATE/SELECT
│
├── vo/
│   └── AnalyzeScheduleVo.java       # 스케줄 파일 한 항목의 메모리 표현
│
└── common/
    ├── constants/AnalyzeConstants.java  # 로그 타입, 분석 레벨, 상태 상수
    ├── exception/AnalyzeException.java  # @Retryable 트리거 예외
    └── util/
        ├── DateUtil.java            # 날짜 포맷 유틸
        └── SequenceHelper.java      # DB Sequence nextval (PostgreSQL/Altibase 분기)
```

---

## 3. 전체 실행 흐름

```
[1] 서버 기동
     │
     ├─ PolicyLoader.load()          ← @PostConstruct
     │   정책 파일(PreCheck_AnalyzePolicy.conf) 전체 읽기
     │   → policyMap { "serverId:logId" → AnalyzePolicy }
     │
     └─ 스케줄러 대기 시작

[2] AnalyzeScheduler.run()           ← @Scheduled fixedDelay=60초
     │
     ├─ getSchedules()               스케줄 파일 캐시 로드 (60초 유효)
     │
     └─ for each schedule:
          shouldRun(schedule, now)?
          ├─ 배치: 지정 요일 + 지정 시각 + 오늘 첫 1회
          └─ 주기: startTime~endTime 사이 intervalMinutes 간격마다

[3] AnalyzeService.analyze(scheduleVo)
     │
     ├─ SEQ_ANALYZE_HISTORY.nextval
     │
     ├─ TB_ANALYZE_HISTORY INSERT    상태=FAIL, failReason="IN_PROGRESS"
     │   (JVM 비정상 종료 시에도 미완료 이력이 DB에 남도록 선등록)
     │
     └─ AnalyzeRetryService.analyzeWithRetry(...)

[4] AnalyzeRetryService.analyzeWithRetry()  ← @Retryable(AnalyzeException, 4회, 300s)
     │
     └─ analyzeInternal(...)
          │
          ├─ [주기 스케줄만] selectLastSuccess()
          │   마지막 SUCCESS 이력의 lastAnalyzeLogId 조회
          │
          ├─ TB_COLLECT_LOG 조회
          │   ├─ 배치: selectForAnalyze(오늘 날짜 전체)
          │   └─ 주기: selectAfterLogId(lastAnalyzeLogId 이후 신규만)
          │
          ├─ for each collectLog:
          │    analyzeOne(collectLog)
          │    │
          │    ├─ policyLoader.findPolicy(serverId, logId)
          │    │   → null이면 LEVEL_UNANALYZED 반환
          │    │
          │    └─ logType에 따라 Analyzer 선택
          │         문구 → PhraseAnalyzer
          │         수치 → NumericAnalyzer
          │         날짜 → DateAnalyzer
          │         존재 → ExistenceAnalyzer
          │         정보 → InfoAnalyzer
          │
          ├─ TB_ANALYZE_RESULT INSERT (건별)
          │
          └─ TB_ANALYZE_HISTORY UPDATE (상태=SUCCESS, 통계)

[5] 실패 시: @Recover.recover()
     TB_ANALYZE_HISTORY UPDATE (상태=FAIL, failReason=예외 메시지)
```

---

## 4. 분석 레벨 판정 기준

| 로그 타입 | 판정 조건 | 결과 레벨 |
|-----------|-----------|-----------|
| 문구(PhraseAnalyzer) | 에러 키워드 포함 | LEVEL_ERROR |
| | 에러 키워드 없음 | LEVEL_NORMAL |
| 수치(NumericAnalyzer) | 조건식 불만족 또는 logValue null | LEVEL_ERROR |
| | 조건식 만족 + 임계치 근접(warningRatio%) | LEVEL_WARNING |
| | 조건식 만족 + 경고 구간 밖 | LEVEL_NORMAL |
| 날짜(DateAnalyzer) | 로그 내 yyyy/MM/dd가 오늘과 다름, 또는 날짜 없음 | LEVEL_ERROR |
| | 로그 내 yyyy/MM/dd가 오늘과 일치 | LEVEL_NORMAL |
| 존재(ExistenceAnalyzer) | 로그 존재 자체가 에러 | LEVEL_ERROR |
| 정보(InfoAnalyzer) | 항상 | LEVEL_INFO |
| 정책 미등록 | logId가 정책 파일에 없음 | LEVEL_UNANALYZED |

**수치형 경고 구간 계산 (`<`, `<=` 연산자):**
```
warningDelta = threshold × warningRatio / 100
경고 구간 = [threshold - warningDelta, threshold)
예) threshold=100, warningRatio=20 → logValue가 80 이상 100 미만이면 LEVEL_WARNING
```

**수치형 경고 구간 계산 (`>`, `>=` 연산자):**
```
경고 구간 = (threshold, threshold + warningDelta]
예) threshold=10, warningRatio=20 → logValue가 10 초과 12 이하이면 LEVEL_WARNING
```

---

## 5. 설정 파일

### application-{profile}.yml

```yaml
precheck:
  analyze:
    policy-file-path:   # 분석 정책 파일 경로 (미설정 시 {user.home}/cfg/PreCheck_AnalyzePolicy.conf)
    schedule-file-path: # 분석 스케줄 파일 경로 (미설정 시 {user.home}/cfg/PreCheck_AnalyzeLogs_Schedule.conf)
    scheduler:
      reload-interval-ms: 60000   # 스케줄 파일 캐시 유효 시간(ms)
```

| 항목 | 로딩 시점 | 클래스 |
|------|-----------|--------|
| 정책 파일 | 서버 시작 1회 | `PolicyLoader` |
| 스케줄 파일 | 60초마다 (캐시) | `AnalyzeScheduler.getSchedules()` |

---

### 분석 정책 파일 (PreCheck_AnalyzePolicy.conf)

```
# 형식: [serverId][logId][로그타입][타입별 파라미터...]

[서버1][ERRLINE][수치][<=][0][1]
# ↑serverId  ↑logId  ↑타입  ↑연산자 ↑임계치 ↑경고비율(%)

[서버1][SYS_MSG][문구][에러,실패,오류,ERROR,Exception]
# ↑serverId  ↑logId  ↑타입  ↑에러키워드목록(콤마구분)

[서버1][BDAY][날짜]        # 파라미터 없음
[서버1][FILE_LOCK][존재]   # 파라미터 없음
[서버1][DAILY_SUMMARY][정보] # 파라미터 없음
```

- `#`으로 시작하는 줄은 주석
- 같은 `serverId:logId`가 중복되면 **마지막 정의**가 적용됨
- 파싱 실패 라인은 WARN 로그 기록 후 스킵 (예외 없음)

---

### 분석 스케줄 파일 (PreCheck_AnalyzeLogs_Schedule.conf)

```
# 배치 형식: [serverId][sourceFilePath][배치|daySpec|HHmmss]
[서버1][/logs/system.log][배치|1-5|090000]
# → 월~금(1-5) 09:00:00에 배치 분석 1회 실행

# 주기 형식: [serverId][sourceFilePath][주기|daySpec|HHmmss|간격(분)|HHmmss]
[서버1][/logs/system.log][주기|1-5|090000|30|180000]
# → 월~금(1-5) 09:00~18:00 사이 30분 간격으로 반복 실행

# daySpec 표기:
#   *     = 매일
#   1     = 특정 요일 하나 (0=일, 1=월, ..., 6=토)
#   1-5   = 요일 범위 (월~금)
```

- 같은 `serverId+sourceFilePath` 중복 시 **마지막 정의** 적용
- `sourceFilePath` 생략 가능 → `[serverId][배치|...]` 형식도 허용

---

## 6. 재시도 동작

```
analyzeWithRetry() 호출
  │
  ├─ 1차 시도
  │   AnalyzeException 발생 → 300초(5분) 대기
  │
  ├─ 2차 재시도
  │   AnalyzeException 발생 → 300초 대기
  │
  ├─ 3차 재시도
  │   AnalyzeException 발생 → 300초 대기
  │
  ├─ 4차 재시도 (maxAttempts=4)
  │   AnalyzeException 발생
  │
  └─ @Recover.recover() 호출
      TB_ANALYZE_HISTORY UPDATE → 상태=FAIL
```

`AnalyzeException` 이외의 예외도 내부에서 `AnalyzeException`으로 래핑하여 재시도 대상이 된다.

---

## 7. DB 테이블 관계

```
TB_COLLECT_LOG (수집 서버가 INSERT)
  collectLogId (PK)
  serverId, sourceFilePath
  logType, logId
  logContent, logValue, logTimestamp
  collectDate
        │
        │  분석 서버가 읽기
        ▼
TB_ANALYZE_RESULT (분석 서버가 INSERT)
  analyzeResultId (PK, SEQ_ANALYZE_RESULT)
  collectLogId (FK)
  analyzeLevel   ← 정상/경고/에러/정보/미분석
  analyzeMessage
  thresholdValue, thresholdOperator, warningRatio  ← 수치형만 사용
  notifyYn = 'N'  ← 통보 서버가 'Y'로 갱신

TB_ANALYZE_HISTORY (분석 서버가 INSERT/UPDATE)
  analyzeHistoryId (PK, SEQ_ANALYZE_HISTORY)
  serverId, sourceFilePath
  analyzeStatus  ← SUCCESS / FAIL (IN_PROGRESS는 failReason으로 표시)
  totalCount, successCount, errorCount, warningCount
  lastAnalyzeLogId  ← 주기 스케줄의 재개 지점
```

---

## 8. 코드 읽기 추천 순서

1. **`AnalyzeConstants.java`** — 상수 전체를 먼저 파악 (로그 타입, 레벨, 상태)
2. **`domain/` 패키지** — DTO와 Policy 도메인 모델 구조 파악
3. **`config/PolicyLoader.java`** — 정책 파일이 어떻게 메모리에 올라가는지
4. **`analyzer/NumericAnalyzer.java`** — 가장 복잡한 분석 로직 (경고 구간 계산)
5. **`service/AnalyzeRetryService.java`** — 핵심 분석 루프 (`analyzeInternal`)
6. **`service/AnalyzeService.java`** — 이력 선등록 패턴 이해
7. **`scheduler/AnalyzeScheduler.java`** — 스케줄 판별 로직 (`shouldRunBatch`, `shouldRunPeriodic`)
8. **`parser/` 패키지** — 설정 파일 포맷 파싱 규칙
