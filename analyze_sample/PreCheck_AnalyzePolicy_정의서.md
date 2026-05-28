# PreCheck 로그 분석 정책 정의서 v1.0 (테스트용)

> 작성일: 2026-05-28  
> 기반 샘플: `monitoring_120_log.0519` (dlprem01), `monitoring_141_log.0519` (axistuja)  
> 목적: 분석 서버 테스트용 정책 — 모든 입력 타입과 연산자 케이스를 망라

---

## ⚠️ 중요: LOG_ID 표준화 필요 사항

`monitoring_141_log.0519` (axistuja 서버)의 원본 LOG_ID들이 **로그포맷정의서 규칙을 위반**하고 있음.  
레거시 담당자에게 아래 매핑 기준으로 **LOG_ID 수정을 요청**해야 함.

| 원본 LOG_ID (위반) | 표준화 LOG_ID | 위반 사유 |
|---|---|---|
| `생성일자` | `DATE_CONF_GEN` | 한글 사용 금지 |
| `DISK 사용률` | `DISK_USAGE` | 공백, 한글 금지 |
| `현물 주체 0~9` | `SPOT_SUBJ_0~9` | 공백, 한글 금지 |
| `파생 주체 1` | `DERV_SUBJ_1` | 공백, 한글 금지 |
| `계좌정보   1` | `ACCT_INFO_1` | 공백, 한글 금지 |
| `실시간정산 0~9` | `REALTIME_SETTLE_0~9` | 공백, 한글 금지 |
| `MOM_RV수` | `MOM_RV_CNT` | 한글 금지 |
| `az_pro_nm` | `PROC_AZ` | 소문자 금지 |
| `ao_mp_pro_nm` | `PROC_AO_MP` | 소문자 금지 |
| `ao_jn_pro_nm` | `PROC_AO_JN` | 소문자 금지 |
| `ao_tr_pro_nm` | `PROC_AO_TR` | 소문자 금지 |
| `batchk체크` | `BATCHK_CHECK` | 소문자, 한글 금지 |
| `주문체결파일갯수` | `ORDER_FILE_CNT` | 한글 금지 |
| `XAGENT 파생 처리` | `XAGENT_DERV` | 공백, 한글 금지 |
| `XAGENT 계좌 처리` | `XAGENT_ACCT` | 공백, 한글 금지 |
| `SACCTMAST처리내역` | `DB_SACCTMAST` | 소문자, 한글 금지 |
| `FACCTMAST처리내역` | `DB_FACCTMAST` | 소문자, 한글 금지 |
| `SACCTADJT처리내역` | `DB_SACCTADJT` | 소문자, 한글 금지 |
| `FACCTADJT처리내역` | `DB_FACCTADJT` | 소문자, 한글 금지 |
| `SJANGMAST처리내역` | `DB_SJANGMAST` | 소문자, 한글 금지 |
| `FJANGMAST처리내역` | `DB_FJANGMAST` | 소문자, 한글 금지 |
| `IICLEAR_END FILE` | `FILE_IICLEAR_END` | 공백 금지 |

---

## 정책 총 목록

| # | 서버구분 | LOG_ID | 타입 | 세부 설정 | 설명 |
|---|---|---|---|---|---|
| 1 | dlprem01-테스트개발 | ERRLINE | 수치 | <=, 0, 1% | 에러라인수 0 이하 정상 |
| 2 | dlprem01-테스트개발 | V_MCOMM_REQUEST | 수치 | >=, 100000, 20% | 요청건수 10만 이상 정상 |
| 3 | dlprem01-테스트개발 | V_MCOMM_RESPONSE | 수치 | >=, 300000, 20% | 응답건수 30만 이상 정상 |
| 4 | dlprem01-테스트개발 | BDAY | 날짜 | - | 영업일 날짜 오늘 일치 |
| 5 | dlprem01-테스트개발 | JBATCH | 정보 | - | 장내종목 BATCH 수신 이력 |
| 6 | dlprem01-테스트개발 | KBATCH | 정보 | - | 장외종목 BATCH 수신 이력 |
| 7 | dlprem01-테스트개발 | JBATCH_N | 정보 | - | NXT 장내 BATCH 이력 |
| 8 | dlprem01-테스트개발 | JBATCH_T | 정보 | - | 통합 장내 BATCH 이력 |
| 9 | dlprem01-테스트개발 | KBATCH_N | 정보 | - | NXT 장외 BATCH 이력 |
| 10 | dlprem01-테스트개발 | KBATCH_T | 정보 | - | 통합 장외 BATCH 이력 |
| 11 | dlprem01-테스트개발 | SWOBTCH | 정보 | - | 주식위클리옵션 BATCH 이력 |
| 12 | dlprem01-테스트개발 | ONBOOT | 정보 | - | 투자정보 개시 이력 |
| 13 | dlprem01-테스트개발 | DAYCLS | 정보 | - | 영업일변경 이력 |
| 14 | dlprem01-테스트개발 | DAYCHG | 정보 | - | 일변경 이력 |
| 15 | dlprem01-테스트개발 | DAYOPN | 정보 | - | 영업일 업무개시 이력 |
| 16 | dlprem01-테스트개발 | SYS_MSG | 문구 | 에러,실패,오류,FAIL,ERROR,Exception,timeout | 시스템 에러 문구 감시 |
| 17 | dlprem01-테스트개발 | CONN_STATUS | 문구 | Connection refused,TIMEOUT,refused,denied | 접속 에러 문구 감시 |
| 18 | dlprem01-테스트개발 | PROC_MIN | 수치 | >=, 5, 20% | 최소 프로세스 수 확인 |
| 19 | dlprem01-테스트개발 | MCAST_FLAG | 수치 | =, 1, 1% | MCAST 플래그 정확값 확인 |
| 20 | dlprem01-테스트개발 | QUEUE_SIZE | 수치 | <=, 50000, 10% | 큐 사이즈 최댓값 제한 |
| 21 | dlprem01-테스트개발 | SESSION_CNT | 수치 | >, 0, 50% | 세션 1개 이상 확인 |
| 22 | dlprem01-테스트개발 | DATE_REPORT | 날짜 | - | 리포트 날짜 확인 |
| 23 | dlprem01-테스트개발 | FILE_LOCK | 존재 | - | 락 파일 부재 에러 |
| 24 | dlprem01-테스트개발 | MEM_CHECK | 존재 | - | 메모리 부재 에러 |
| 25 | dlprem01-테스트개발 | DAILY_SUMMARY | 정보 | - | 일일 요약 Dashboard용 |
| 26 | dlprem01-테스트개발 | TRADE_VOLUME | 정보 | - | 거래량 Dashboard 그래프용 |
| 27 | axistuja-자동주문 | DATE_CONF_GEN | 날짜 | - | 설정파일 생성일자 확인 |
| 28 | axistuja-자동주문 | JCM_CONF | 정보 | - | JCM 설정파일 경로 이력 |
| 29 | axistuja-자동주문 | MCAST | 정보 | - | MCAST 상태 이력 |
| 30 | axistuja-자동주문 | BATCHK_CHECK | 정보 | - | batchk 파일 체크 이력 |
| 31 | axistuja-자동주문 | DISK_USAGE | 수치 | <, 85, 20% | 디스크 85% 미만 정상 |
| 32 | axistuja-자동주문 | PROC_AZ | 수치 | >=, 3, 33% | az 프로세스 3개 이상 정상 |
| 33 | axistuja-자동주문 | PROC_AO_MP | 수치 | >=, 3, 33% | ao_mp 프로세스 확인 |
| 34 | axistuja-자동주문 | PROC_AO_JN | 수치 | >=, 1, 50% | ao_jn 프로세스 확인 |
| 35 | axistuja-자동주문 | PROC_AO_TR | 수치 | >=, 4, 25% | ao_tr 프로세스 확인 |
| 36 | axistuja-자동주문 | MOM_RV_CNT | 수치 | >=, 1, 50% | MOM_RV 수신 확인 |
| 37 | axistuja-자동주문 | SIGNAL_SHUTDOWN | 수치 | <=, 1, 1% | 비정상 종료 프로세스 수 |
| 38 | axistuja-자동주문 | SPOT_SUBJ_0 | 수치 | >=, 2000, 20% | 현물주체0 수신 합산 |
| 39 | axistuja-자동주문 | SPOT_SUBJ_1 | 수치 | >=, 1, 50% | 현물주체1 최소 수신 |
| 40 | axistuja-자동주문 | SPOT_SUBJ_2 | 수치 | >=, 2000, 20% | 현물주체2 수신량 |
| 41 | axistuja-자동주문 | SPOT_SUBJ_3 | 수치 | >=, 1000, 20% | 현물주체3 수신량 |
| 42 | axistuja-자동주문 | SPOT_SUBJ_4 | 수치 | >=, 1, 50% | 현물주체4 최소 수신 |
| 43 | axistuja-자동주문 | SPOT_SUBJ_5 | 수치 | >=, 1, 50% | 현물주체5 최소 수신 |
| 44 | axistuja-자동주문 | SPOT_SUBJ_6 | 수치 | >=, 10, 30% | 현물주체6 수신량 |
| 45 | axistuja-자동주문 | SPOT_SUBJ_7 | 정보 | - | 현물주체7 Dashboard용 |
| 46 | axistuja-자동주문 | SPOT_SUBJ_8 | 정보 | - | 현물주체8 Dashboard용 |
| 47 | axistuja-자동주문 | SPOT_SUBJ_9 | 정보 | - | 현물주체9 Dashboard용 |
| 48 | axistuja-자동주문 | DERV_SUBJ_1 | 정보 | - | 파생주체1 Dashboard용 |
| 49 | axistuja-자동주문 | ACCT_INFO_1 | 수치 | >=, 50, 30% | 계좌정보 수신량 확인 |
| 50 | axistuja-자동주문 | REALTIME_SETTLE_0 | 수치 | >=, 2000, 30% | 실시간정산 채널0 |
| 51 | axistuja-자동주문 | REALTIME_SETTLE_1 | 수치 | >=, 3000, 30% | 실시간정산 채널1 |
| 52 | axistuja-자동주문 | REALTIME_SETTLE_2 | 수치 | >=, 3000, 30% | 실시간정산 채널2 |
| 53 | axistuja-자동주문 | REALTIME_SETTLE_3 | 수치 | >=, 3000, 30% | 실시간정산 채널3 |
| 54 | axistuja-자동주문 | REALTIME_SETTLE_4 | 수치 | >=, 2000, 30% | 실시간정산 채널4 |
| 55 | axistuja-자동주문 | REALTIME_SETTLE_5 | 수치 | >=, 5000, 20% | 실시간정산 채널5 |
| 56 | axistuja-자동주문 | REALTIME_SETTLE_6 | 수치 | >=, 2000, 30% | 실시간정산 채널6 |
| 57 | axistuja-자동주문 | REALTIME_SETTLE_7 | 수치 | >=, 2000, 30% | 실시간정산 채널7 |
| 58 | axistuja-자동주문 | REALTIME_SETTLE_8 | 수치 | >=, 4000, 20% | 실시간정산 채널8 |
| 59 | axistuja-자동주문 | REALTIME_SETTLE_9 | 수치 | >=, 3000, 30% | 실시간정산 채널9 |
| 60 | axistuja-자동주문 | ORDER_FILE_CNT | 수치 | >=, 20, 25% | 주문체결 파일 개수 |
| 61 | axistuja-자동주문 | XAGENT_DERV | 정보 | - | XAGENT 파생 처리 이력 |
| 62 | axistuja-자동주문 | XAGENT_ACCT | 수치 | >=, 50, 30% | XAGENT 계좌 처리량 |
| 63 | axistuja-자동주문 | DB_SACCTMAST | 수치 | <=, 0, 1% | SACCTMAST 오류 0 정상 |
| 64 | axistuja-자동주문 | DB_FACCTMAST | 수치 | <=, 0, 1% | FACCTMAST 오류 0 정상 |
| 65 | axistuja-자동주문 | DB_SACCTADJT | 수치 | <=, 0, 1% | SACCTADJT 오류 0 정상 |
| 66 | axistuja-자동주문 | DB_FACCTADJT | 수치 | <=, 0, 1% | FACCTADJT 오류 0 정상 |
| 67 | axistuja-자동주문 | DB_SJANGMAST | 수치 | <=, 0, 1% | SJANGMAST 오류 0 정상 |
| 68 | axistuja-자동주문 | DB_FJANGMAST | 수치 | <=, 0, 1% | FJANGMAST 오류 0 정상 |
| 69 | axistuja-자동주문 | FILE_IICLEAR_END | 존재 | - | IICLEAR_END 파일 부재 에러 |
| 70 | axistuja-자동주문 | SYS_MSG | 문구 | 에러,실패,오류,FAIL,ERROR,Exception | 시스템 에러 문구 감시 |
| 71 | axistuja-자동주문 | DB_CONN_STATUS | 문구 | ORA-,Altibase error,Connection failed,timeout | DB 접속 에러 감시 |
| 72 | axistuja-자동주문 | PROC_MIN | 수치 | >=, 10, 20% | 최소 프로세스 수 |
| 73 | axistuja-자동주문 | MCAST_FLAG | 수치 | =, 1, 1% | MCAST 플래그 확인 |
| 74 | axistuja-자동주문 | QUEUE_SIZE | 수치 | <=, 10000, 15% | 큐 사이즈 제한 |
| 75 | axistuja-자동주문 | SESSION_CNT | 수치 | >, 0, 50% | 세션 존재 확인 |
| 76 | axistuja-자동주문 | DATE_BATCH | 날짜 | - | 배치 날짜 확인 |
| 77 | axistuja-자동주문 | FILE_PIDLOCK | 존재 | - | PID 락 파일 부재 에러 |
| 78 | axistuja-자동주문 | SHM_CHECK | 존재 | - | 공유메모리 부재 에러 |
| 79 | axistuja-자동주문 | DAILY_SUMMARY | 정보 | - | 일일 요약 Dashboard용 |
| 80 | axistuja-자동주문 | TRADE_VOLUME | 정보 | - | 거래량 Dashboard 그래프용 |

---

## 입력 타입별 정책 수 요약

| 입력 타입 | dlprem01 건수 | axistuja 건수 | 합계 |
|---|---|---|---|
| 수치 | 7 | 29 | 36 |
| 정보 | 12 | 12 | 24 |
| 날짜 | 2 | 2 | 4 |
| 문구 | 2 | 2 | 4 |
| 존재 | 2 | 4 | 6 |
| **합계** | **25** | **49** | **80** |

---

## 수치형 연산자 사용 케이스 요약

| 연산자 | 의미 | 사용 LOG_ID 예시 |
|---|---|---|
| `<=` | 이하여야 정상 (최댓값 제한) | ERRLINE, QUEUE_SIZE, DB_*, SIGNAL_SHUTDOWN |
| `>=` | 이상이어야 정상 (최솟값 보장) | V_MCOMM_*, PROC_*, SPOT_SUBJ_*, REALTIME_SETTLE_* |
| `<` | 미만이어야 정상 | DISK_USAGE |
| `>` | 초과여야 정상 | SESSION_CNT |
| `=` | 정확한 값이어야 정상 | MCAST_FLAG |

---

## 판정 결과 예시

```
[정상] [DISK_USAGE] DISK 사용률 41 < 85(임계치)
[경고] [DISK_USAGE] DISK 사용률 74 < 85(임계치 대비 20% 근접)
[에러] [DISK_USAGE] DISK 사용률 90 >= 85(임계치)

[정상] [ERRLINE] ## Number of Error Line : 0 <= 0(임계치)
[에러] [ERRLINE] ## Number of Error Line : 3 > 0(임계치)

[정상] [BDAY] bday[2026/05/28] frec[12543] krec[3] (오늘 날짜 일치)
[에러] [BDAY] bday[2026/05/19] frec[12543] krec[3] (날짜 불일치: 기대=2026/05/28, 실제=2026/05/19)

[에러] [FILE_IICLEAR_END] IICLEAR_END FILE이 없습니다.

[정상] [SYS_MSG] 시스템 정상 기동 완료
[에러] [SYS_MSG] 시스템 Connection refused 발생 (키워드: refused)

[정보] [JBATCH] 05-19 09:00:39 종료 [JBATCH]- 장내종목 BATCH 수신완료

[미분석] [UNKNOWN_LOG_ID] 분석 정책 미등록
```

---

## 운영 적용 시 주의사항

1. **레거시 담당자 협의 필수**: 표준화된 LOG_ID로 로그 수정 요청 (141 서버 전체)
2. **임계치는 초기값**: 실제 운영 데이터를 보며 조정 필요
3. **정책 변경 시 분석 서버 재기동 필요**
4. **미분석 LOG_ID 확인**: 초기 운영 시 미분석 로그를 모니터링하며 정책 추가
5. **실시간정산/현물주체 임계치**: 장중 시간대와 장외 시간대 구분 필요 (향후 개선)
