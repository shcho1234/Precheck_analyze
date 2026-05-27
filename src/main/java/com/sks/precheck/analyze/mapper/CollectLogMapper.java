package com.sks.precheck.analyze.mapper;

import com.sks.precheck.analyze.domain.CollectLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CollectLogMapper {

    List<CollectLog> selectForAnalyze(
            @Param("collectDate") String collectDate,
            @Param("serverId") String serverId,
            @Param("sourceFilePath") String sourceFilePath
    );

    // 주기 스케줄 전용 — afterCollectLogId 이후 신규 로그만 조회하여 중복 분석 방지
    List<CollectLog> selectAfterLogId(
            @Param("collectDate") String collectDate,
            @Param("serverId") String serverId,
            @Param("sourceFilePath") String sourceFilePath,
            @Param("afterCollectLogId") Long afterCollectLogId
    );
}
