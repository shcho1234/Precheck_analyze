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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 로그 분석 진입점 — 분석 이력(TB_ANALYZE_HISTORY) 선등록 후 AnalyzeRetryService에 위임
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

    @Async("analyzeTaskExecutor")
    public void analyze(AnalyzeScheduleVo scheduleVo) {
        String scheduleType = parseScheduleType(scheduleVo.getScheduleType());

        Long historyId = sequenceHelper.nextval("SEQ_ANALYZE_HISTORY");
        LocalDateTime now = LocalDateTime.now();

        String analyzeTargetDate = DateUtil.todayAnalyzeDate();
        String analyzeDate = analyzeTargetDate;

        AnalyzeHistory history = new AnalyzeHistory();
        history.setAnalyzeHistoryId(historyId);
        history.setServerId(scheduleVo.getServerId());
        history.setAnalyzeTargetDate(analyzeTargetDate);
        history.setSourceFilePath(scheduleVo.getSourceFilePath());
        // 초기 상태를 FAIL/IN_PROGRESS로 선등록 — JVM 비정상 종료 시 해당 실행이 미완료임을 DB에 남김
        // 분석 성공 시 AnalyzeRetryService가 STATUS_SUCCESS로 UPDATE한다
        history.setAnalyzeStatus(AnalyzeConstants.STATUS_FAIL);
        history.setFailReason("IN_PROGRESS");
        history.setAnalyzeStartAt(now);
        history.setAnalyzeDate(analyzeDate);
        history.setCreatedAt(now);
        history.setUpdatedAt(now);
        analyzeHistoryMapper.insert(history);
        log.info("분석 이력 등록 - historyId: {}, serverId: {}, date: {}", historyId, scheduleVo.getServerId(), analyzeTargetDate);

        analyzeRetryService.analyzeWithRetry(historyId, scheduleVo, scheduleType, analyzeTargetDate, analyzeDate);
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
