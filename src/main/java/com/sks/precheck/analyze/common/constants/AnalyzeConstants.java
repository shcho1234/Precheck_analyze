package com.sks.precheck.analyze.common.constants;

public final class AnalyzeConstants {

    public static final int MAX_RETRY_COUNT = 3;
    public static final long RETRY_DELAY_MILLISECONDS = 300_000L;

    public static final String ANALYZE_DATE_FORMAT = "yyyyMMdd";
    public static final String LOG_TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

    public static final String LOG_TYPE_TEXT = "문구";
    public static final String LOG_TYPE_INFO = "정보";
    public static final String LOG_TYPE_DATE = "날짜";
    public static final String LOG_TYPE_NUMERIC = "수치";
    public static final String LOG_TYPE_EXIST = "존재";
    public static final String LOG_TYPE_COMPARE = "비교";
    public static final String LOG_TYPE_TIME = "시간";

    public static final String LEVEL_NORMAL = "정상";
    public static final String LEVEL_WARNING = "경고";
    public static final String LEVEL_ERROR = "에러";
    public static final String LEVEL_INFO = "정보";
    public static final String LEVEL_UNANALYZED = "미분석";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAIL = "FAIL";
    public static final String STATUS_PARTIAL = "PARTIAL";

    private AnalyzeConstants() {
    }
}
