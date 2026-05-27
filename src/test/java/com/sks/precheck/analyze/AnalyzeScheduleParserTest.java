package com.sks.precheck.analyze;

import static org.junit.jupiter.api.Assertions.*;

import com.sks.precheck.analyze.parser.AnalyzeScheduleParser;
import com.sks.precheck.analyze.vo.AnalyzeScheduleVo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AnalyzeScheduleParserTest {

    private final AnalyzeScheduleParser parser = new AnalyzeScheduleParser();

    @TempDir
    Path tempDir;

    @Test
    void parse_serverUnit() throws Exception {
        Path file = tempDir.resolve("PreCheck_AnalyzeLogs_Schedule.conf");
        Files.writeString(file, "[dlprem01-테스트개발][배치|1-5|093001]\n", StandardCharsets.UTF_8);

        List<AnalyzeScheduleVo> schedules = parser.parseScheduleFile(file.toString());
        assertEquals(1, schedules.size());

        AnalyzeScheduleVo vo = schedules.get(0);
        assertEquals("dlprem01-테스트개발", vo.getServerId());
        assertNull(vo.getSourceFilePath());
        assertEquals("배치", vo.getScheduleType());
        assertEquals("1-5", vo.getDayOfWeek());
        assertEquals("093001", vo.getStartTime());
        assertNull(vo.getIntervalMinutes());
        assertNull(vo.getEndTime());
    }

    @Test
    void parse_fileUnit() throws Exception {
        Path file = tempDir.resolve("PreCheck_AnalyzeLogs_Schedule.conf");
        Files.writeString(file, "[dlprem01-테스트개발][/tmp/check.out][주기|*|090001|10|100001]\n", StandardCharsets.UTF_8);

        List<AnalyzeScheduleVo> schedules = parser.parseScheduleFile(file.toString());
        assertEquals(1, schedules.size());

        AnalyzeScheduleVo vo = schedules.get(0);
        assertEquals("dlprem01-테스트개발", vo.getServerId());
        assertEquals("/tmp/check.out", vo.getSourceFilePath());
        assertEquals("주기", vo.getScheduleType());
        assertEquals("*", vo.getDayOfWeek());
        assertEquals("090001", vo.getStartTime());
        assertEquals(10, vo.getIntervalMinutes());
        assertEquals("100001", vo.getEndTime());
    }

    @Test
    void parse_invalidLine_isIgnored() throws Exception {
        Path file = tempDir.resolve("PreCheck_AnalyzeLogs_Schedule.conf");
        Files.writeString(file, "[dlprem01-테스트개발][주기|*|090001|10]\n", StandardCharsets.UTF_8);

        List<AnalyzeScheduleVo> schedules = parser.parseScheduleFile(file.toString());
        assertTrue(schedules.isEmpty());
    }
}

