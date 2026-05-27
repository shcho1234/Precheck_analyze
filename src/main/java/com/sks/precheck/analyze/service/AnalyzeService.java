package com.sks.precheck.analyze.service;

import com.sks.precheck.analyze.common.constants.AnalyzeConstants;
import com.sks.precheck.analyze.common.exception.AnalyzeException;
import com.sks.precheck.analyze.common.util.DateUtil;
import com.sks.precheck.analyze.common.util.SequenceHelper;
import com.sks.precheck.analyze.domain.AnalyzeHistory;
import com.sks.precheck.analyze.mapper.AnalyzeHistoryMapper;
import com.sks.precheck.analyze.vo.AnalyzeScheduleVo;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * 로그 분석 서비스
 *
 * <p>분석 스케줄러로부터 호출되어 로그 분석을 시작하는 진입점입니다.
 * 역할:
 * - 분석 이력(TB_ANALYZE_HISTORY) 선등록 (IN_PROGRESS 상태)
 * - 실제 분석 처리를 AnalyzeRetryService로 위임 (@Retryable로 5분 간격 3회 재시도)
 * - 분석 완료 후 이력 업데이트
 *
 * <p>명세서 흐름 (로그 분석 서버):
 * 1. 스케줄러가 정해진 시간/주기에 이 클래스 호출
 * 2. 이 클래스는 분석 이력 선등록 후 AnalyzeRetryService에 위임
 * 3. AnalyzeRetryService가 @Retryable로 실제 분석 수행 (실패 시 5분 간격 재시도)
 * 4. 분석 결과를 TB_ANALYZE_RESULT에 INSERT
 * 5. TB_ANALYZE_HISTORY 업데이트 (분석 건수, 상태 등)
 *
 * @see AnalyzeRetryService 실제 분석 로직
 * @see AnalyzeScheduler 스케줄러 (매 1분마다 shouldRun 판별 후 호출)
 */
@Service
public class AnalyzeService {

    private static final Logger log = LogManager.getLogger(AnalyzeService.class);

    private final SequenceHelper sequenceHelper;
    private final AnalyzeHistoryMapper analyzeHistoryMapper;
    private final AnalyzeRetryService analyzeRetryService;

    public AnalyzeService(
            SequenceHelper sequenceHelper,
            AnalyzeHistoryMapper analyzeHistoryMapper,
            AnalyzeRetryService analyzeRetryService
    ) {
        this.sequenceHelper = sequenceHelper;
        this.analyzeHistoryMapper = analyzeHistoryMapper;
        this.analyzeRetryService = analyzeRetryService;
    }

    /**
     * 로그 분석 진입점
     *
     * <p>분석 스케줄 정보를 받아 분석을 시작합니다.
     * 프로세스:
     * 1. 스케줄 타입 검증 ("배치" 또는 "주기")
     * 2. SEQ_ANALYZE_HISTORY 시퀀스로 분석 이력 ID 생성
     * 3. TB_ANALYZE_HISTORY에 IN_PROGRESS 상태로 선등록
     *    (분석 도중 실패해도 이력이 DB에 남도록 함)
     * 4. AnalyzeRetryService.analyzeWithRetry()에 위임
     *    - 이 메서드는 @Retryable이므로 실패 시 5분 간격 3회 자동 재시도
     *    - 최종 성공/실패 여부를 반환
     *
     * <p>주의: 이 메서드는 이력 선등록만 하고, 실제 분석은 AnalyzeRetryService에서 수행합니다.
     *
     * @param scheduleVo 분석 스케줄 정보
     * @return 분석 결과 (성공한 로그 건수)
     * @throws AnalyzeException 스케줄 타입이 유효하지 않을 경우
     */
    public int analyze(AnalyzeScheduleVo scheduleVo) {
        // 1. 스케줄 타입 검증 ("배치" 또는 "주기")
        String scheduleType = parseScheduleType(scheduleVo.getScheduleType());

        // 2. SEQ_ANALYZE_HISTORY 시퀀스로 분석 이력 ID 생성
        Long historyId = sequenceHelper.nextval("SEQ_ANALYZE_HISTORY");
        LocalDateTime now = LocalDateTime.now();

        // 3. 분석 대상 날짜 (현재 날짜, yyyyMMdd 형식)
        String analyzeTargetDate = DateUtil.todayAnalyzeDate();
        String analyzeDate = analyzeTargetDate;

        // 4. TB_ANALYZE_HISTORY 선등록 (IN_PROGRESS 상태)
        //    분석 중 장애 발생 시에도 이력이 DB에 남음
        AnalyzeHistory history = new AnalyzeHistory();
        history.setAnalyzeHistoryId(historyId);
        history.setServerId(scheduleVo.getServerId());
        history.setAnalyzeTargetDate(analyzeTargetDate);
        history.setSourceFilePath(scheduleVo.getSourceFilePath());
        history.setAnalyzeStatus(AnalyzeConstants.STATUS_FAIL);   // 초기 상태: FAIL
        history.setFailReason("IN_PROGRESS");                    // 분석 진행 중
        history.setAnalyzeStartAt(now);
        history.setAnalyzeDate(analyzeDate);
        history.setCreatedAt(now);
        history.setUpdatedAt(now);
        analyzeHistoryMapper.insert(history);
        log.info("분석 이력 선등록 - historyId: {}, serverId: {}, date: {}", historyId, scheduleVo.getServerId(), analyzeTargetDate);

        // 5. 실제 분석을 AnalyzeRetryService에 위임
        //    @Retryable이므로 실패 시 5분 간격 3회 자동 재시도
        return analyzeRetryService.analyzeWithRetry(historyId, scheduleVo, scheduleType, analyzeTargetDate, analyzeDate);
    }

    private String parseScheduleType(String scheduleType) {
        if (scheduleType == null || scheduleType.isBlank()) {
            throw new AnalyzeException("분석 스케줄 타입이 비어있다");
        }

        String type = scheduleType.trim();
        if ("배치".equals(type) || "주기".equals(type)) {
            return type;
        }
        throw new AnalyzeException("분석 스케줄 타입 오류: " + scheduleType);
    }
}
