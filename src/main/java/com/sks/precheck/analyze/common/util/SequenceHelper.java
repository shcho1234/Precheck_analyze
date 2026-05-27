package com.sks.precheck.analyze.common.util;

import com.sks.precheck.analyze.common.exception.AnalyzeException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
/**
 * SEQUENCE 헬퍼 유틸리티
 *
 * <p>DB 벤더(PostgreSQL, Altibase 등)에 따라 SEQUENCE nextval을 조회하는 SQL이 달라지므로,
 * {@link #nextval(String)}에서 런타임에 DB 제품명을 확인하여 알맞은 쿼리를 생성합니다.
 * 이 유틸은 서비스 계층에서 SEQ_ANALYZE_HISTORY, SEQ_ANALYZE_RESULT 등의 시퀀스 값을
 * 미리 조회할 때 사용됩니다.
 */
public class SequenceHelper {

    private final DataSource dataSource;

    public SequenceHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long nextval(String sequenceName) {
        try (Connection connection = dataSource.getConnection()) {
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            String sql = buildNextvalSql(dbProductName, sequenceName);

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AnalyzeException("SEQUENCE nextval 조회 결과가 없다: " + sequenceName);
                }
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new AnalyzeException("SEQUENCE nextval 조회 실패: " + sequenceName, e);
        }
    }

    private String buildNextvalSql(String dbProductName, String sequenceName) {
        if (dbProductName != null && dbProductName.toLowerCase().contains("postgres")) {
            return "select nextval('" + sequenceName + "')";
        }
        return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
    }
}
