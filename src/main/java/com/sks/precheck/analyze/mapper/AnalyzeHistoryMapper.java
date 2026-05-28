package com.sks.precheck.analyze.mapper;

import com.sks.precheck.analyze.domain.AnalyzeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyzeHistoryMapper {

    int insert(AnalyzeHistory analyzeHistory);

    int update(AnalyzeHistory analyzeHistory);

    // 주기 스케줄 시작점 확인용 — 당일(analyzeDate) 마지막 SUCCESS 이력의 lastAnalyzeLogId를 읽어 어디까지 처리했는지 파악
    AnalyzeHistory selectLastSuccess(
            @Param("serverId") String serverId,
            @Param("sourceFilePath") String sourceFilePath,
            @Param("analyzeDate") String analyzeDate
    );
}
