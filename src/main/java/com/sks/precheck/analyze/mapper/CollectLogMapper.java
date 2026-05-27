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

    List<CollectLog> selectAfterLogId(
            @Param("collectDate") String collectDate,
            @Param("serverId") String serverId,
            @Param("sourceFilePath") String sourceFilePath,
            @Param("afterCollectLogId") Long afterCollectLogId
    );
}
