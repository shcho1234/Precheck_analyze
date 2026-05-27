package com.sks.precheck.analyze.mapper;

import com.sks.precheck.analyze.domain.AnalyzeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyzeHistoryMapper {

    int insert(AnalyzeHistory analyzeHistory);

    int update(AnalyzeHistory analyzeHistory);

    AnalyzeHistory selectLastSuccess(
            @Param("serverId") String serverId,
            @Param("sourceFilePath") String sourceFilePath
    );
}
