package com.sks.precheck.analyze.mapper;

import com.sks.precheck.analyze.domain.AnalyzeResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnalyzeResultMapper {

    int insert(AnalyzeResult analyzeResult);
}
